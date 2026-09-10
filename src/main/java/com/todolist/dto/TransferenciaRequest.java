package com.todolist.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferenciaRequest {

    @NotNull
    private Long contaOrigemId;

    @NotNull
    private Long contaDestinoId;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal valor;

    private String descricao;
}
