package com.todolist.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrcamentoItemResponse {
    private Long categoriaId;
    private String categoriaNome;
    private String categoriaCor;
    private String categoriaIcone;
    private BigDecimal limite;
    private BigDecimal gasto;
    private BigDecimal restante;
    private Double percentualUsado;
    private Boolean estourado;
}
