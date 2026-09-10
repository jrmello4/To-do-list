package com.todolist.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "metas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Meta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String categoria = "GERAL";

    @Column(name = "valor_alvo", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorAlvo;

    @Column(name = "valor_atual", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal valorAtual = BigDecimal.ZERO;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String unidade = "R$";

    private LocalDate prazo;

    @Column(nullable = false, length = 10)
    @Builder.Default
    private String cor = "#10b981";

    @Column(length = 50)
    @Builder.Default
    private String icone = "target";

    @Column(nullable = false)
    @Builder.Default
    private Boolean concluida = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean ativo = true;

    @Column(name = "auto_aporte_percentual", precision = 5, scale = 2)
    private BigDecimal autoAportePercentual;

    @Column(name = "auto_aporte_ativo", nullable = false)
    @Builder.Default
    private Boolean autoAporteAtivo = false;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_atualizacao", nullable = false)
    private LocalDateTime dataAtualizacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User usuario;

    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
        dataAtualizacao = LocalDateTime.now();
        if (valorAtual == null) valorAtual = BigDecimal.ZERO;
        if (concluida == null) concluida = false;
        if (ativo == null) ativo = true;
    }

    @PreUpdate
    protected void onUpdate() {
        dataAtualizacao = LocalDateTime.now();
    }
}
