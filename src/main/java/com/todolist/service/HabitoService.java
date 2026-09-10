package com.todolist.service;

import com.todolist.dto.HabitoRequest;
import com.todolist.dto.HabitoResponse;
import com.todolist.entity.Habito;
import com.todolist.entity.RegistroHabito;
import com.todolist.entity.User;
import com.todolist.exception.ResourceNotFoundException;
import com.todolist.repository.HabitoRepository;
import com.todolist.repository.RegistroHabitoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HabitoService {

    private final HabitoRepository habitoRepository;
    private final RegistroHabitoRepository registroHabitoRepository;
    private final AuthService authService;

    @Transactional
    public List<HabitoResponse> listarTodos() {
        User user = authService.obterUsuarioAutenticado();
        List<Habito> habitos = habitoRepository.findByUsuarioIdAndAtivoTrueOrderByNomeAsc(user.getId());

        if (habitos.isEmpty()) {
            // Hábitos iniciais sugeridos
            List<Habito> padroes = List.of(
                    Habito.builder().nome("Beber 2L de Água").icone("droplet").cor("#06b6d4").ativo(true).usuario(user).build(),
                    Habito.builder().nome("Exercício Físico / Caminhada").icone("activity").cor("#10b981").ativo(true).usuario(user).build(),
                    Habito.builder().nome("Leitura / Estudo 20min").icone("book-open").cor("#8b5cf6").ativo(true).usuario(user).build()
            );
            habitos = habitoRepository.saveAll(padroes);
        }

        return habitos.stream().map(this::paraResponse).collect(Collectors.toList());
    }

    @Transactional
    public HabitoResponse toggleHoje(Long id) {
        User user = authService.obterUsuarioAutenticado();
        Habito habito = habitoRepository.findByIdAndUsuarioId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Hábito", id));

        LocalDate hoje = LocalDate.now();
        Optional<RegistroHabito> optReg = registroHabitoRepository.findByHabitoIdAndDataRegistro(id, hoje);

        if (optReg.isPresent()) {
            RegistroHabito reg = optReg.get();
            reg.setConcluido(!reg.getConcluido());
            registroHabitoRepository.save(reg);
        } else {
            RegistroHabito novo = RegistroHabito.builder()
                    .habito(habito)
                    .dataRegistro(hoje)
                    .concluido(true)
                    .build();
            registroHabitoRepository.save(novo);
        }

        return paraResponse(habito);
    }

    @Transactional
    public HabitoResponse criar(HabitoRequest request) {
        User user = authService.obterUsuarioAutenticado();
        Habito habito = Habito.builder()
                .nome(request.getNome().trim())
                .icone(request.getIcone() != null ? request.getIcone() : "check")
                .cor(request.getCor() != null ? request.getCor() : "#6366f1")
                .ativo(true)
                .usuario(user)
                .build();

        return paraResponse(habitoRepository.save(habito));
    }

    @Transactional
    public void excluir(Long id) {
        User user = authService.obterUsuarioAutenticado();
        Habito habito = habitoRepository.findByIdAndUsuarioId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Hábito", id));
        habito.setAtivo(false);
        habitoRepository.save(habito);
    }

    public HabitoResponse paraResponse(Habito h) {
        LocalDate hoje = LocalDate.now();
        List<RegistroHabito> registros = registroHabitoRepository.findByHabitoIdOrderByDataRegistroDesc(h.getId());

        boolean concluidoHoje = registros.stream()
                .anyMatch(r -> r.getDataRegistro().isEqual(hoje) && Boolean.TRUE.equals(r.getConcluido()));

        // Cálculo de Streak
        int streak = 0;
        LocalDate cursor = concluidoHoje ? hoje : hoje.minusDays(1);

        while (true) {
            final LocalDate checkData = cursor;
            boolean bateu = registros.stream()
                    .anyMatch(r -> r.getDataRegistro().isEqual(checkData) && Boolean.TRUE.equals(r.getConcluido()));
            if (bateu) {
                streak++;
                cursor = cursor.minusDays(1);
            } else {
                break;
            }
        }

        return HabitoResponse.builder()
                .id(h.getId())
                .nome(h.getNome())
                .icone(h.getIcone())
                .cor(h.getCor())
                .ativo(h.getAtivo())
                .concluidoHoje(concluidoHoje)
                .streakDias(streak)
                .build();
    }
}