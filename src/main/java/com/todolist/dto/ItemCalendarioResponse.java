package com.todolist.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemCalendarioResponse {
    private String id;
    private Long origemId;
    private String tipo; // TAREFA, RECEITA, DESPESA, EVENTO
    private String titulo;
    private LocalDate data;
    private LocalTime hora;
    private BigDecimal valor;
    private String status; // CONCLUIDA, PENDENTE, PAGO, AGENDADO
    private String cor;
    private String detalhe;
}
