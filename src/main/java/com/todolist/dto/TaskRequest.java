package com.todolist.dto;

import com.todolist.entity.Categoria;
import com.todolist.entity.Prioridade;
import com.todolist.entity.Recorrencia;
import com.todolist.entity.StatusTarefa;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskRequest {

    @NotBlank(message = "O título é obrigatório")
    @Size(max = 200, message = "O título deve ter no máximo 200 caracteres")
    private String titulo;

    @Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres")
    private String descricao;

    private Boolean concluida;

    private Prioridade prioridade;

    private StatusTarefa status;

    private Categoria categoria;

    private LocalDate dataVencimento;

    private Recorrencia recorrencia;

    private Integer pomodorosEstimados;

    private List<SubtaskRequest> subtarefas;

    private List<Long> tagIds;
}
