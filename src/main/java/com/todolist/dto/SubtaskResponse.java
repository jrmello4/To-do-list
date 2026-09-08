package com.todolist.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubtaskResponse {

    private Long id;
    private String titulo;
    private Boolean concluida;
    private LocalDateTime dataCriacao;
}
