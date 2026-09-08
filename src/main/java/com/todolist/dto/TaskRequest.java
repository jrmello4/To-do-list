package com.todolist.dto;

import com.todolist.entity.Prioridade;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(name = "TaskRequest", description = "Dados enviados para criar ou atualizar uma tarefa")
public class TaskRequest {

    @NotBlank(message = "O título é obrigatório")
    @Size(max = 200, message = "O título deve ter no máximo 200 caracteres")
    @Schema(
            description = "Título da tarefa. Campo obrigatório.",
            example = "Estudar Spring Boot",
            maxLength = 200,
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String titulo;

    @Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres")
    @Schema(
            description = "Detalhamento opcional da tarefa.",
            example = "Aprofundar em JPA e Flyway",
            maxLength = 1000,
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String descricao;

    @Schema(
            description = "Indica se a tarefa já foi concluída. Quando omitido, assume false na criação "
                    + "e mantém o valor atual na atualização.",
            example = "false",
            defaultValue = "false",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Boolean concluida;

    @Schema(
            description = "Projeto ao qual a tarefa pertence. Nulo coloca a tarefa na caixa de entrada.",
            example = "1",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Long projetoId;

    @Schema(
            description = "Data limite, no formato AAAA-MM-DD. Sem hora de propósito: prazo é uma "
                    + "decisão sobre o dia.",
            example = "2026-09-30",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private LocalDate prazo;

    @Schema(
            description = "Prioridade da tarefa",
            example = "ALTA",
            defaultValue = "MEDIA",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Prioridade prioridade;

    @Schema(
            description = "Identificadores das etiquetas da tarefa. Substitui as atuais: enviar "
                    + "lista vazia remove todas; omitir o campo mantém as que já existem.",
            example = "[1, 2]",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private List<Long> etiquetaIds;
}
