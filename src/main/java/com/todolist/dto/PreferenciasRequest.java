package com.todolist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "PreferenciasRequest", description = "Preferências de lembrete da conta")
public class PreferenciasRequest {

    @Schema(description = "Se a conta quer receber o resumo diário", example = "true")
    private Boolean lembretesAtivos;

    @Min(value = 0, message = "A hora deve estar entre 0 e 23")
    @Max(value = 23, message = "A hora deve estar entre 0 e 23")
    @Schema(description = "Hora local em que o resumo deve chegar", example = "8")
    private Integer horaLembrete;

    @Size(max = 60)
    @Schema(description = "Fuso da conta, no padrão IANA. A interface envia o do navegador.",
            example = "America/Sao_Paulo")
    private String fusoHorario;
}
