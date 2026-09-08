package com.todolist.dto;

import com.todolist.entity.Categoria;
import com.todolist.entity.Prioridade;
import com.todolist.entity.Recorrencia;
import com.todolist.entity.StatusTarefa;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskResponse {

    private Long id;
    private String titulo;
    private String descricao;
    private Boolean concluida;
    private Prioridade prioridade;
    private StatusTarefa status;
    private Categoria categoria;
    private LocalDate dataVencimento;
    private Boolean estaAtrasada;
    private Boolean deletada;
    private LocalDateTime dataDelecao;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;
    private List<SubtaskResponse> subtarefas;
    private int totalSubtarefas;
    private int subtarefasConcluidas;
    private Recorrencia recorrencia;
    private Integer pomodorosEstimados;
    private Integer pomodorosRealizados;
    private List<TagResponse> tags;
    private List<AttachmentResponse> anexos;
}
