package com.todolist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "EtiquetaResponse", description = "Etiqueta da conta autenticada")
public class EtiquetaResponse {

    @Schema(description = "Identificador da etiqueta", example = "1")
    private Long id;

    @Schema(description = "Nome da etiqueta", example = "urgente-casa")
    private String nome;

    @Schema(description = "Cor da etiqueta", example = "rosa")
    private String cor;

    @Schema(description = "Quantas tarefas pendentes usam esta etiqueta", example = "4")
    private long tarefasPendentes;
}
