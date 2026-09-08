package com.todolist.dto;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskSummaryResponse implements Serializable {

    private long total;
    private long concluidas;
    private long pendentes;
    private long aFazer;
    private long emAndamento;
    private long atrasadas;
    private long naLixeira;
}
