package com.todolist.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertaEsporteResponse {
    private Long eventoId;
    private String titulo;
    private String descricao;
    private LocalDate data;
    private LocalTime hora;
    private Long minutosParaInicio;
    private String tipo;
    private String mensagem;
}
