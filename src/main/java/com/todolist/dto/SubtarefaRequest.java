package com.todolist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "SubtarefaRequest", description = "Dados de um passo da tarefa")
public class SubtarefaRequest {

    @Size(max = 200, message = "O título deve ter no máximo 200 caracteres")
    @Schema(
            description = "Texto do passo. Obrigatório na criação; na alteração, "
                    + "omitir mantém o texto atual.",
            example = "Levantar as referências",
            maxLength = 200)
    private String titulo;

    @Schema(
            description = "Se o passo já foi cumprido. Omitir mantém o estado atual.",
            example = "true")
    private Boolean concluida;
}
