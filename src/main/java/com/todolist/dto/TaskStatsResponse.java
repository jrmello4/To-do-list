package com.todolist.dto;

import lombok.*;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskStatsResponse implements Serializable {

    private Map<String, Long> porCategoria;
    private Map<String, Long> porPrioridade;
    private List<String> ultimos7DiasRotulos;
    private List<Long> ultimos7DiasCriadas;
    private List<Long> ultimos7DiasConcluidas;
    private double taxaConclusaoNoPrazo;
    private int totalPomodorosRealizados;
    private long totalRecorrentes;
}
