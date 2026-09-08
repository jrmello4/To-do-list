package com.todolist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "ConclusaoRequest", description = "Nova situação de conclusão da tarefa")
public class ConclusaoRequest {

    @NotNull(message = "Informe se a tarefa está concluída")
    @Schema(description = "true conclui a tarefa, false a reabre. Explícito em vez de alternar, "
            + "para a mesma chamada repetida dar sempre o mesmo resultado.",
            example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean concluida;
}
