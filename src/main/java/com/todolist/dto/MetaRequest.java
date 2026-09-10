package com.todolist.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetaRequest {

    @NotBlank(message = "O título da meta é obrigatório")
    private String titulo;

    private String descricao;

    private String categoria;

    @NotNull(message = "O valor alvo é obrigatório")
    @DecimalMin(value = "0.01", message = "O valor alvo deve ser maior que zero")
    private BigDecimal valorAlvo;

    private BigDecimal valorAtual;

    private String unidade;

    private LocalDate prazo;

    private String cor;

    private String icone;
}
