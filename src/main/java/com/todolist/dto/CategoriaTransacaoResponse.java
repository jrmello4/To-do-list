package com.todolist.dto;

import com.todolist.entity.TipoTransacao;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoriaTransacaoResponse {
    private Long id;
    private String nome;
    private TipoTransacao tipo;
    private String icone;
    private String cor;
    private BigDecimal limiteMensal;
}