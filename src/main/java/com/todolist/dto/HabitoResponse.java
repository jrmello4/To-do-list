package com.todolist.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HabitoResponse {
    private Long id;
    private String nome;
    private String icone;
    private String cor;
    private Boolean ativo;
    private Boolean concluidoHoje;
    private int streakDias;
}