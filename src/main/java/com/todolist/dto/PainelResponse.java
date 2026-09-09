package com.todolist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "PainelResponse", description = "Números da conta, agregados no banco")
public class PainelResponse {

    @Schema(description = "As mesmas contagens de /api/tarefas/resumo")
    private ResumoResponse resumo;

    @Schema(description = "Concluídas por dia, do mais antigo ao mais recente. "
            + "Dias sem conclusão aparecem com zero, para o gráfico não pular datas.")
    private List<PontoDoDia> concluidasPorDia;

    @Schema(description = "Pendentes por projeto. A caixa de entrada entra como \"Sem projeto\".")
    private List<Contagem> pendentesPorProjeto;

    @Schema(description = "Pendentes por prioridade")
    private List<Contagem> pendentesPorPrioridade;

    @Schema(description = "Horas médias entre criar e concluir, nas últimas 200 concluídas. "
            + "Nula quando ainda não há nenhuma.", example = "36.5")
    private Double horasMediasParaConcluir;

    @Schema(description = "Sequências dos hábitos")
    private List<SequenciaDeHabito> habitos;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "PontoDoDia", description = "Quantidade concluída num dia")
    public static class PontoDoDia {

        @Schema(description = "O dia", example = "2026-09-08")
        private LocalDate data;

        @Schema(description = "Quantas tarefas foram concluídas", example = "3")
        private long quantidade;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "Contagem", description = "Rótulo, cor e quantidade")
    public static class Contagem {

        @Schema(description = "Nome exibido", example = "Faculdade")
        private String rotulo;

        @Schema(description = "Cor da paleta, quando houver", example = "verde")
        private String cor;

        @Schema(description = "Quantidade", example = "7")
        private long quantidade;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "SequenciaDeHabito", description = "Sequência atual e recorde de um hábito")
    public static class SequenciaDeHabito {

        @Schema(description = "Nome do hábito", example = "Ler 20 páginas")
        private String nome;

        @Schema(description = "Cor do hábito", example = "violeta")
        private String cor;

        @Schema(description = "Sequência atual", example = "12")
        private int atual;

        @Schema(description = "Maior sequência", example = "21")
        private int recorde;
    }
}
