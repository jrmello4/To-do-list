package com.todolist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @Size(max = 150, message = "O título deve ter no máximo 150 caracteres")
    private String titulo;

    private String descricao;

    @NotNull(message = "A data do evento é obrigatória")
    private LocalDate dataEvento;

    private LocalTime horaInicio;

    private LocalTime horaFim;

    @Size(max = 10, message = "A cor deve ter no máximo 10 caracteres")
    private String cor;

    @Size(max = 50, message = "A categoria deve ter no máximo 50 caracteres")
    private String categoria;
}
