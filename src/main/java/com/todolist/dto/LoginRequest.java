package com.todolist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "LoginRequest", description = "Credenciais de acesso")
public class LoginRequest {

    @NotBlank(message = "O e-mail é obrigatório")
    @Schema(description = "E-mail da conta", example = "ana@exemplo.com",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "A senha é obrigatória")
    @Schema(description = "Senha da conta", example = "uma-senha-forte",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String senha;
}
