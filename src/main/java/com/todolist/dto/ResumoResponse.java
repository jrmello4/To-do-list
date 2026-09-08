package com.todolist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "ResumoResponse", description = "Contagens da conta, calculadas no banco")
public class ResumoResponse {

    @Schema(description = "Total de tarefas", example = "42")
    private long total;

    @Schema(description = "Tarefas pendentes", example = "17")
    private long pendentes;

    @Schema(description = "Tarefas concluídas", example = "25")
    private long concluidas;

    @Schema(description = "Pendentes com prazo já vencido", example = "3")
    private long atrasadas;

    @Schema(description = "Pendentes que vencem na data de referência", example = "2")
    private long vencemHoje;

    @Schema(description = "Percentual concluído, arredondado", example = "60")
    private int percentualConcluido;
}
