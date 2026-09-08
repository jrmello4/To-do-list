package com.todolist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "TaskResponse", description = "Representação de uma tarefa retornada pela API")
public class TaskResponse {

    @Schema(description = "Identificador único da tarefa", example = "1")
    private Long id;

    @Schema(description = "Título da tarefa", example = "Estudar Spring Boot")
    private String titulo;

    @Schema(description = "Detalhamento da tarefa", example = "Aprofundar em JPA e Flyway")
    private String descricao;

    @Schema(description = "Indica se a tarefa já foi concluída", example = "false")
    private Boolean concluida;

    @Schema(description = "Data e hora em que a tarefa foi criada", example = "2026-06-26T10:00:00")
    private LocalDateTime dataCriacao;

    @Schema(description = "Data e hora da última alteração", example = "2026-06-26T10:00:00")
    private LocalDateTime dataAtualizacao;
}
