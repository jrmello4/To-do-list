package com.todolist.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjecaoResponse {
    private LocalDate inicio;
    private LocalDate fim;
    private BigDecimal saldoAtual;
    private BigDecimal receitasPrevistas;
    private BigDecimal despesasPrevistas;
    private BigDecimal saldoProjetado;
    private List<Ponto> pontos;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Ponto {
        private LocalDate data;
        private BigDecimal saldoAcumulado;
        private BigDecimal receitasDoDia;
        private BigDecimal despesasDoDia;
    }
}
