package com.todolist.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "preferencias_esportivas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreferenciaEsporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String esporte; // UFC, FUTEBOL, F1, BASQUETE, NFL, OUTRO

    @Column(name = "nome_interesse", nullable = false, length = 100)
    private String nomeInteresse; // ex: "UFC", "Flamengo", "Fórmula 1", "Lakers"

    @Column(length = 20)
    @Builder.Default
    private String icone = "⚽";

    @Column(length = 10)
    @Builder.Default
    private String cor = "#10b981";

    @Column(nullable = false)
    @Builder.Default
    private Boolean ativo = true;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User usuario;

    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
        if (ativo == null) ativo = true;
    }
}
