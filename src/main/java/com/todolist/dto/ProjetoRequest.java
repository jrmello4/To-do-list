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
@Schema(name = "ProjetoRequest", description = "Dados para criar ou atualizar um projeto")
public class ProjetoRequest {

    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres")
    @Schema(description = "Nome do projeto. Único dentro da conta.", example = "Faculdade",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String nome;

    @Size(max = 20, message = "A cor deve ter no máximo 20 caracteres")
    @Schema(description = "Identificador de cor da paleta da interface",
            example = "verde", defaultValue = "indigo")
    private String cor;

    @Schema(description = "Projeto arquivado sai da barra lateral sem apagar as tarefas",
            example = "false", defaultValue = "false")
    private Boolean arquivado;
}
