package com.todolist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "EtiquetaRequest", description = "Dados para criar ou atualizar uma etiqueta")
public class EtiquetaRequest {

    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 60, message = "O nome deve ter no máximo 60 caracteres")
    @Schema(description = "Nome da etiqueta. Único dentro da conta.", example = "urgente-casa",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String nome;

    @Size(max = 20, message = "A cor deve ter no máximo 20 caracteres")
    @Schema(description = "Identificador de cor da paleta da interface",
            example = "rosa", defaultValue = "indigo")
    private String cor;
}
