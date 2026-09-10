package com.todolist.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categorias_transacoes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoriaTransacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoTransacao tipo;

    @Column(length = 50)
    @Builder.Default
    private String icone = "tag";

    @Column(nullable = false, length = 10)
    @Builder.Default
    private String cor = "#6366f1";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User usuario;
}