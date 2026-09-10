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
@Schema(name = "TrocaDeSenhaRequest", description = "Dados para trocar a senha da conta")
public class TrocaDeSenhaRequest {

    @NotBlank(message = "A senha atual é obrigatória")
    @Schema(description = "Senha em uso. Pedida mesmo já havendo token: um token roubado não "
            + "deve bastar para trocar a senha e tomar a conta.",
            example = "a-senha-de-agora", requiredMode = Schema.RequiredMode.REQUIRED)
    private String senhaAtual;

    @NotBlank(message = "A nova senha é obrigatória")
    @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres")
    @Schema(description = "Nova senha, de no mínimo 8 caracteres.",
            example = "uma-senha-nova-forte", requiredMode = Schema.RequiredMode.REQUIRED)
    private String novaSenha;
}
