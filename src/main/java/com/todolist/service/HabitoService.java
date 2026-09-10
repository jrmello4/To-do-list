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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HabitoService {

    /** Janela máxima para cálculo de streak (evita full table scan). */
    private static final int JANELA_STREAK_DIAS = 365;

    private final HabitoRepository habitoRepository;
    private final RegistroHabitoRepository registroHabitoRepository;
    private final AuthService authService;

    @Transactional(readOnly = true)
    public List<HabitoResponse> listarTodos() {
        User user = authService.obterUsuarioAutenticado();
        List<Habito> habitos = habitoRepository.findByUsuarioIdAndAtivoTrueOrderByNomeAsc(user.getId());
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
        LocalDate inicio = hoje.minusDays(JANELA_STREAK_DIAS);
        Set<LocalDate> datasConcluidas = registroHabitoRepository
                .findByHabitoIdAndDataRegistroGreaterThanEqualAndConcluidoTrue(h.getId(), inicio)
                .stream()
                .map(RegistroHabito::getDataRegistro)
                .collect(Collectors.toSet());

        boolean concluidoHoje = datasConcluidas.contains(hoje);

        int streak = 0;
        LocalDate cursor = concluidoHoje ? hoje : hoje.minusDays(1);
        while (datasConcluidas.contains(cursor) && streak < JANELA_STREAK_DIAS) {
            streak++;
            cursor = cursor.minusDays(1);
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
