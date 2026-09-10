package com.todolist.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotaRapidaRequest {
    private String titulo;
    private String conteudo;
    private String cor;
    private Boolean fixada;
}