package com.todolist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "AuthResponse", description = "Token emitido após cadastro ou login")
public class AuthResponse {

    @Schema(description = "Token JWT. Envie em cada requisição no cabeçalho "
            + "Authorization, no formato \"Bearer <token>\".",
            example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @Schema(description = "Esquema do cabeçalho Authorization", example = "Bearer")
    @Builder.Default
    private String tipo = "Bearer";

    @Schema(description = "Validade do token, em segundos", example = "86400")
    private long expiraEm;

    @Schema(description = "Conta autenticada")
    private UsuarioResponse usuario;
}
