package com.todolist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "SubtarefaResponse", description = "Um passo da tarefa")
public class SubtarefaResponse {

    @Schema(description = "Identificador do passo", example = "1")
    private Long id;

    @Schema(description = "Texto do passo", example = "Levantar as referências")
    private String titulo;

    @Schema(description = "Se o passo já foi cumprido", example = "false")
    private Boolean concluida;

    @Schema(description = "Posição na lista, começando em 0", example = "0")
    private Integer ordem;
}
