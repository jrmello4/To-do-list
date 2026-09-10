package com.todolist.dto;

import com.todolist.entity.TipoConta;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContaResponse {
    private Long id;
    private String nome;
    private TipoConta tipo;
    private BigDecimal saldoInicial;
    private BigDecimal saldoAtual;
    private String cor;
    private Boolean ativo;
}