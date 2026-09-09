package com.todolist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "UsuarioResponse", description = "Dados públicos de uma conta")
public class UsuarioResponse {

    @Schema(description = "Identificador da conta", example = "1")
    private Long id;

    @Schema(description = "Nome de quem usa a conta", example = "Ana Ribeiro")
    private String nome;

    @Schema(description = "E-mail da conta", example = "ana@exemplo.com")
    private String email;

    @Schema(description = "Quando a conta foi criada", example = "2026-09-08T10:00:00")
    private LocalDateTime dataCriacao;

    @Schema(description = "Se a conta recebe o resumo diário", example = "false")
    private Boolean lembretesAtivos;

    @Schema(description = "Hora local do resumo", example = "8")
    private Integer horaLembrete;

    @Schema(description = "Fuso da conta", example = "America/Sao_Paulo")
    private String fusoHorario;
}
