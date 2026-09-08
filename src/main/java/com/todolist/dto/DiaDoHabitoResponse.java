package com.todolist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;

/** Um quadradinho da grade de sequência. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "DiaDoHabitoResponse", description = "Situação de um dia na grade do hábito")
public class DiaDoHabitoResponse {

    @Schema(description = "O dia", example = "2026-09-08")
    private LocalDate data;

    @Schema(description = "Se o hábito vale neste dia da semana", example = "true")
    private boolean aplicavel;

    @Schema(description = "Se foi cumprido", example = "true")
    private boolean feito;
}
