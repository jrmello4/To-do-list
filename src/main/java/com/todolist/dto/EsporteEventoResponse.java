package com.todolist.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EsporteEventoResponse {
    private String id;
    private String esporte;
    private String icone;
    private String titulo;
    private String subtitulo;
    private LocalDate data;
    private LocalTime hora;
    private String dataHoraFormatada;
    private String transmissao;
    private String status; // AGENDADO, AO VIVO, ENCERRADO
    private String resultado;
    private Boolean noCalendario;
    private Long eventoCalendarioId;
}
