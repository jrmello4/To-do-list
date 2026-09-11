package com.todolist.dto;

import com.todolist.entity.StatusTransacao;
import com.todolist.entity.TipoTransacao;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransacaoResponse {
    private Long id;
    private String descricao;
    private TipoTransacao tipo;
    private BigDecimal valor;
    private LocalDate dataVencimento;
    private LocalDate dataPagamento;
    private StatusTransacao status;
    private Boolean estaAtrasada;
    private Boolean parcelado;
    private Integer numeroParcela;
    private Integer totalParcelas;
    private String grupoParcelaId;
    private Boolean transferencia;
    private String observacoes;
    private Long contaId;
    private String contaNome;
    private String contaCor;
    private Long categoriaId;
    private String categoriaNome;
    private String categoriaIcone;
    private String categoriaCor;
}