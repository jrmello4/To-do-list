package com.todolist.service;

import com.todolist.dto.HabitoResponse;
import com.todolist.dto.PainelResponse;
import com.todolist.dto.ProjetoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.todolist.repository.TaskRepository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PainelService {

    /** Quantas conclusões recentes entram na média de tempo. */
    private static final int AMOSTRA_DE_TEMPOS = 200;

    private static final Map<String, String> COR_DA_PRIORIDADE = Map.of(
            "BAIXA", "azul",
            "MEDIA", "indigo",
            "ALTA", "ambar",
            "URGENTE", "rosa");

    private final TaskRepository taskRepository;
    private final FusoDaConta fusoDaConta;
    private final TaskService taskService;
    private final ProjetoService projetoService;
    private final HabitoService habitoService;

    @Transactional(readOnly = true)
    public PainelResponse montar(Long usuarioId, LocalDate referencia, int dias) {
        // O fuso decide em que coluna do gráfico cada conclusão cai.
        ZoneId zona = fusoDaConta.de(usuarioId);
        LocalDate hoje = fusoDaConta.hoje(usuarioId, referencia);
        int janela = Math.min(Math.max(dias, 7), 365);

        return PainelResponse.builder()
                .resumo(taskService.resumo(usuarioId, hoje))
                .concluidasPorDia(concluidasPorDia(usuarioId, zona, hoje, janela))
                .pendentesPorProjeto(pendentesPorProjeto(usuarioId))
                .pendentesPorPrioridade(pendentesPorPrioridade(usuarioId))
                .horasMediasParaConcluir(horasMediasParaConcluir(usuarioId))
                .habitos(sequencias(usuarioId, hoje))
                .build();
    }

    /**
     * Série contínua: os dias sem conclusão vêm com zero. Devolver só os dias
     * com registro faria o gráfico comprimir os intervalos vazios e mentir
     * sobre o ritmo.
     *
     * O dia de cada conclusão é o dia no fuso da conta. As bordas da janela
     * fazem o caminho inverso — da meia-noite local para o instante em UTC —
     * porque é assim que dataConclusao está gravada.
     */
    private List<PainelResponse.PontoDoDia> concluidasPorDia(Long usuarioId, ZoneId zona,
                                                             LocalDate hoje, int dias) {
        LocalDate inicio = hoje.minusDays(dias - 1L);

        LocalDateTime inicioUtc = emUtc(inicio.atStartOfDay(zona));
        LocalDateTime fimUtc = emUtc(hoje.plusDays(1).atStartOfDay(zona));

        Map<LocalDate, Long> porDia = new HashMap<>();
        for (LocalDateTime conclusao
                : taskRepository.buscarConclusoesEntre(usuarioId, inicioUtc, fimUtc)) {
            LocalDate diaLocal = conclusao.atOffset(ZoneOffset.UTC)
                    .atZoneSameInstant(zona)
                    .toLocalDate();
            porDia.merge(diaLocal, 1L, Long::sum);
        }

        List<PainelResponse.PontoDoDia> serie = new ArrayList<>();
        for (LocalDate dia = inicio; !dia.isAfter(hoje); dia = dia.plusDays(1)) {
            serie.add(PainelResponse.PontoDoDia.builder()
                    .data(dia)
                    .quantidade(porDia.getOrDefault(dia, 0L))
                    .build());
        }

        return serie;
    }

    private List<PainelResponse.Contagem> pendentesPorProjeto(Long usuarioId) {
        List<PainelResponse.Contagem> contagens = new ArrayList<>();

        for (ProjetoResponse projeto : projetoService.listar(usuarioId)) {
            if (projeto.getTarefasPendentes() > 0) {
                contagens.add(PainelResponse.Contagem.builder()
                        .rotulo(projeto.getNome())
                        .cor(projeto.getCor())
                        .quantidade(projeto.getTarefasPendentes())
                        .build());
            }
        }

        // A caixa de entrada não é projeto, mas some do gráfico se ficar de fora.
        long emProjetos = contagens.stream()
                .mapToLong(PainelResponse.Contagem::getQuantidade).sum();
        long pendentes = taskRepository.countByUsuarioId(usuarioId)
                - taskRepository.countByUsuarioIdAndConcluidaTrue(usuarioId);
        long semProjeto = pendentes - emProjetos;

        if (semProjeto > 0) {
            contagens.add(PainelResponse.Contagem.builder()
                    .rotulo("Sem projeto")
                    .quantidade(semProjeto)
                    .build());
        }

        contagens.sort((a, b) -> Long.compare(b.getQuantidade(), a.getQuantidade()));
        return contagens;
    }

    private List<PainelResponse.Contagem> pendentesPorPrioridade(Long usuarioId) {
        List<PainelResponse.Contagem> contagens = new ArrayList<>();

        for (Object[] linha : taskRepository.contarPendentesPorPrioridade(usuarioId)) {
            String prioridade = String.valueOf(linha[0]);
            contagens.add(PainelResponse.Contagem.builder()
                    .rotulo(prioridade)
                    .cor(COR_DA_PRIORIDADE.getOrDefault(prioridade, "indigo"))
                    .quantidade(((Number) linha[1]).longValue())
                    .build());
        }

        return contagens;
    }

    private Double horasMediasParaConcluir(Long usuarioId) {
        List<Object[]> tempos = taskRepository.buscarTemposDeConclusao(
                usuarioId, PageRequest.of(0, AMOSTRA_DE_TEMPOS));

        if (tempos.isEmpty()) {
            return null;
        }

        long minutos = 0;
        for (Object[] par : tempos) {
            minutos += Duration.between((LocalDateTime) par[0], (LocalDateTime) par[1]).toMinutes();
        }

        return Math.round(minutos / (double) tempos.size() / 60 * 10) / 10.0;
    }

    private static LocalDateTime emUtc(java.time.ZonedDateTime instante) {
        return instante.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }

    private List<PainelResponse.SequenciaDeHabito> sequencias(Long usuarioId, LocalDate hoje) {
        return habitoService.listar(usuarioId, hoje).stream()
                .map(habito -> PainelResponse.SequenciaDeHabito.builder()
                        .nome(habito.getNome())
                        .cor(habito.getCor())
                        .atual(habito.getSequenciaAtual())
                        .recorde(habito.getMaiorSequencia())
                        .build())
                .toList();
    }
}
