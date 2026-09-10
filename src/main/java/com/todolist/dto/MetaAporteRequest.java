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
public class MetaAporteRequest {

    @NotNull(message = "O valor do aporte é obrigatório")
    @DecimalMin(value = "0.01", message = "O aporte deve ser maior que zero")
    private BigDecimal valorAporte;
}
