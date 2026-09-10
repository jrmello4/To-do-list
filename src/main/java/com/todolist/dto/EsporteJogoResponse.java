package com.todolist.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EsporteJogoResponse {
    private String timeCasa;
    private String timeVisitante;
    private Integer placarCasa;
    private Integer placarVisitante;
    private String horario;
    private String campeonato;
    private String status;
    private String estadio;
}