package com.todolist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/** Projeto como aparece dentro de uma tarefa: o mínimo para desenhar o rótulo. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "ProjetoResumoResponse", description = "Projeto ao qual a tarefa pertence")
public class ProjetoResumoResponse {

    @Schema(description = "Identificador do projeto", example = "1")
    private Long id;

    @Schema(description = "Nome do projeto", example = "Faculdade")
    private String nome;

    @Schema(description = "Cor do projeto", example = "verde")
    private String cor;
}
