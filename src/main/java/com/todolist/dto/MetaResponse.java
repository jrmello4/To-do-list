package com.todolist.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetaResponse {
    private Long id;
    private String titulo;
    private String descricao;
    private String categoria;
    private BigDecimal valorAlvo;
    private BigDecimal valorAtual;
    private String unidade;
    private LocalDate prazo;
    private String cor;
    private String icone;
    private Boolean concluida;
    private Double percentualConcluido;
    private BigDecimal autoAportePercentual;
    private Boolean autoAporteAtivo;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;
}
