package com.todolist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "ProjetoResponse", description = "Projeto da conta autenticada")
public class ProjetoResponse {

    @Schema(description = "Identificador do projeto", example = "1")
    private Long id;

    @Schema(description = "Nome do projeto", example = "Faculdade")
    private String nome;

    @Schema(description = "Cor do projeto", example = "verde")
    private String cor;

    @Schema(description = "Se está arquivado", example = "false")
    private Boolean arquivado;

    @Schema(description = "Quantas tarefas pendentes o projeto tem", example = "3")
    private long tarefasPendentes;

    @Schema(description = "Quando foi criado", example = "2026-09-08T10:00:00")
    private LocalDateTime dataCriacao;
}
