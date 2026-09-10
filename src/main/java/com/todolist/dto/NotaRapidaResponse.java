package com.todolist.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotaRapidaResponse {
    private Long id;
    private String titulo;
    private String conteudo;
    private String cor;
    private Boolean fixada;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;
}