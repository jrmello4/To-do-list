package com.todolist.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    private Long taskId;
    private String titulo;
    private String mensagem;
    private String tipo; // "ATRASADA", "VENCE_HOJE", "URGENTE"
    private LocalDate dataVencimento;
    private String prioridade;
}
