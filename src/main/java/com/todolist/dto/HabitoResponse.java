package com.todolist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "HabitoResponse", description = "Hábito com sequências já calculadas")
public class HabitoResponse {

    @Schema(description = "Identificador do hábito", example = "1")
    private Long id;

    @Schema(description = "Nome do hábito", example = "Ler 20 páginas")
    private String nome;

    @Schema(description = "Cor do hábito", example = "verde")
    private String cor;

    @Schema(description = "Dias da semana em que vale, no padrão ISO", example = "[1, 3, 5]")
    private List<Integer> diasSemana;

    @Schema(description = "Se o hábito está ativo", example = "true")
    private Boolean ativo;

    @Schema(description = "Dias consecutivos cumpridos até hoje. O dia de hoje não "
            + "quebra a sequência enquanto não acaba.", example = "12")
    private int sequenciaAtual;

    @Schema(description = "Maior sequência nos últimos 365 dias", example = "21")
    private int maiorSequencia;

    @Schema(description = "Se o hábito vale no dia de referência", example = "true")
    private boolean aplicavelHoje;

    @Schema(description = "Se já foi cumprido no dia de referência", example = "false")
    private boolean feitoHoje;

    @Schema(description = "Os últimos dias, do mais antigo ao mais recente, para a grade")
    private List<DiaDoHabitoResponse> ultimosDias;
}
