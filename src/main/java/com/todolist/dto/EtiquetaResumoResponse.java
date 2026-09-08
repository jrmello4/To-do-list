package com.todolist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/** Etiqueta como aparece dentro de uma tarefa. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "EtiquetaResumoResponse", description = "Etiqueta aplicada à tarefa")
public class EtiquetaResumoResponse {

    @Schema(description = "Identificador da etiqueta", example = "1")
    private Long id;

    @Schema(description = "Nome da etiqueta", example = "urgente-casa")
    private String nome;

    @Schema(description = "Cor da etiqueta", example = "rosa")
    private String cor;
}
