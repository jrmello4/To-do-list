package com.todolist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoCalendarioRequest {

    @NotBlank(message = "O título do evento é obrigatório")
    private String titulo;

    private String descricao;

    @NotNull(message = "A data do evento é obrigatória")
    private LocalDate dataEvento;

    private LocalTime horaInicio;

    private LocalTime horaFim;

    private String cor;

    private String categoria;
}
