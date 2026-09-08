package com.todolist.service;

import com.todolist.dto.DiaDoHabitoResponse;
import com.todolist.dto.HabitoRequest;
import com.todolist.dto.HabitoResponse;
import com.todolist.entity.Habito;
import com.todolist.entity.HabitoRegistro;
import com.todolist.exception.NomeDeHabitoEmUsoException;
import com.todolist.exception.ResourceNotFoundException;
import com.todolist.repository.HabitoRegistroRepository;
import com.todolist.repository.HabitoRepository;
import com.todolist.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HabitoService {

    private static final String COR_PADRAO = "verde";
    private static final String TODOS_OS_DIAS = "1,2,3,4,5,6,7";

    /** Tamanho da grade devolvida para a interface desenhar a sequência. */
    private static final int DIAS_NA_GRADE = 14;

    private final HabitoRepository habitoRepository;
    private final HabitoRegistroRepository registroRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<HabitoResponse> listar(Long usuarioId, LocalDate referencia) {
        LocalDate hoje = referencia != null ? referencia : LocalDate.now();
        Map<Long, Set<LocalDate>> registros = registrosPorHabito(usuarioId, hoje);

        return habitoRepository.findByUsuarioIdOrderByNomeAsc(usuarioId)
                .stream()
                .map(habito -> toResponse(
                        habito, registros.getOrDefault(habito.getId(), Set.of()), hoje))
                .toList();
    }

    @Transactional
    public HabitoResponse criar(Long usuarioId, HabitoRequest request, LocalDate referencia) {
        String nome = request.getNome().trim();

        if (habitoRepository.existsByUsuarioIdAndNomeIgnoreCase(usuarioId, nome)) {
            throw new NomeDeHabitoEmUsoException(nome);
        }

        Habito habito = habitoRepository.save(Habito.builder()
                .usuario(usuarioRepository.getReferenceById(usuarioId))
                .nome(nome)
                .cor(vazioVira(request.getCor(), COR_PADRAO))
                .diasSemana(paraCsv(request.getDiasSemana()))
                .ativo(request.getAtivo() == null || request.getAtivo())
                .build());

        return toResponse(habito, Set.of(), hojeOu(referencia));
    }

    @Transactional
    public HabitoResponse atualizar(Long usuarioId, Long id, HabitoRequest request,
                                    LocalDate referencia) {
        Habito habito = buscarDoUsuario(usuarioId, id);
        String nome = request.getNome().trim();

        if (habitoRepository.existsByUsuarioIdAndNomeIgnoreCaseAndIdNot(usuarioId, nome, id)) {
            throw new NomeDeHabitoEmUsoException(nome);
        }

        habito.setNome(nome);
        habito.setCor(vazioVira(request.getCor(), habito.getCor()));

        if (request.getDiasSemana() != null && !request.getDiasSemana().isEmpty()) {
            habito.setDiasSemana(paraCsv(request.getDiasSemana()));
        }
        if (request.getAtivo() != null) {
            habito.setAtivo(request.getAtivo());
        }

        LocalDate hoje = hojeOu(referencia);
        return toResponse(habito, registrosDoHabito(usuarioId, id, hoje), hoje);
    }

    @Transactional
    public void deletar(Long usuarioId, Long id) {
        Habito habito = buscarDoUsuario(usuarioId, id);

        registroRepository.deleteByHabitoId(id);
        habitoRepository.delete(habito);
    }

    /**
     * Marcar é idempotente: chamar duas vezes para o mesmo dia não cria dois
     * registros — a restrição de unicidade já impediria, e uma segunda
     * chamada não deveria virar erro.
     */
    @Transactional
    public HabitoResponse marcar(Long usuarioId, Long id, LocalDate data, LocalDate referencia) {
        Habito habito = buscarDoUsuario(usuarioId, id);
        LocalDate hoje = hojeOu(referencia);

        if (data.isAfter(hoje)) {
            throw new IllegalArgumentException(
                    "Não é possível marcar um dia que ainda não chegou");
        }

        if (registroRepository.findByHabitoIdAndData(id, data).isEmpty()) {
            registroRepository.save(HabitoRegistro.builder().habito(habito).data(data).build());
        }

        return toResponse(habito, registrosDoHabito(usuarioId, id, hoje), hoje);
    }

    @Transactional
    public HabitoResponse desmarcar(Long usuarioId, Long id, LocalDate data, LocalDate referencia) {
        Habito habito = buscarDoUsuario(usuarioId, id);
        LocalDate hoje = hojeOu(referencia);

        registroRepository.findByHabitoIdAndData(id, data).ifPresent(registroRepository::delete);

        return toResponse(habito, registrosDoHabito(usuarioId, id, hoje), hoje);
    }

    /* ---------------------------------------------------------- Auxiliares */

    private Habito buscarDoUsuario(Long usuarioId, Long id) {
        return habitoRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Hábito", id));
    }

    private LocalDate hojeOu(LocalDate referencia) {
        return referencia != null ? referencia : LocalDate.now();
    }

    /** Uma consulta para todos os hábitos da conta, em vez de uma por hábito. */
    private Map<Long, Set<LocalDate>> registrosPorHabito(Long usuarioId, LocalDate hoje) {
        return registroRepository
                .buscarDaConta(usuarioId, hoje.minusDays(CalculoDeSequencia.JANELA_EM_DIAS))
                .stream()
                .collect(Collectors.groupingBy(
                        registro -> registro.getHabito().getId(),
                        Collectors.mapping(HabitoRegistro::getData, Collectors.toSet())));
    }

    private Set<LocalDate> registrosDoHabito(Long usuarioId, Long habitoId, LocalDate hoje) {
        return registrosPorHabito(usuarioId, hoje).getOrDefault(habitoId, Set.of());
    }

    private static String vazioVira(String valor, String padrao) {
        return valor == null || valor.isBlank() ? padrao : valor.trim();
    }

    private static String paraCsv(List<Integer> dias) {
        if (dias == null || dias.isEmpty()) {
            return TODOS_OS_DIAS;
        }
        return dias.stream().distinct().sorted().map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    private static HabitoResponse toResponse(Habito habito, Set<LocalDate> feitos, LocalDate hoje) {
        Set<DayOfWeek> dias = habito.diasComoConjunto();

        List<DiaDoHabitoResponse> grade = new ArrayList<>();
        for (int i = DIAS_NA_GRADE - 1; i >= 0; i--) {
            LocalDate dia = hoje.minusDays(i);
            grade.add(DiaDoHabitoResponse.builder()
                    .data(dia)
                    .aplicavel(dias.contains(dia.getDayOfWeek()))
                    .feito(feitos.contains(dia))
                    .build());
        }

        return HabitoResponse.builder()
                .id(habito.getId())
                .nome(habito.getNome())
                .cor(habito.getCor())
                .diasSemana(dias.stream().map(DayOfWeek::getValue).sorted().toList())
                .ativo(habito.getAtivo())
                .sequenciaAtual(CalculoDeSequencia.atual(feitos, dias, hoje))
                .maiorSequencia(CalculoDeSequencia.maior(feitos, dias, hoje))
                .aplicavelHoje(dias.contains(hoje.getDayOfWeek()))
                .feitoHoje(feitos.contains(hoje))
                .ultimosDias(grade)
                .build();
    }
}
