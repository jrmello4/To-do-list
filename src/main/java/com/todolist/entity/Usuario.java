package com.todolist.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, length = 180, unique = true)
    private String email;

    /** Hash BCrypt. Nunca sai da aplicação: nenhum DTO expõe este campo. */
    @Column(name = "senha_hash", nullable = false, length = 100)
    private String senhaHash;

    @Column(nullable = false)
    @Builder.Default
    private Boolean ativo = true;

    /** Fuso da conta. O servidor roda em UTC; o dia de quem usa é outro. */
    @Column(name = "fuso_horario", nullable = false, length = 60)
    @Builder.Default
    private String fusoHorario = "America/Sao_Paulo";

    @Column(name = "lembretes_ativos", nullable = false)
    @Builder.Default
    private Boolean lembretesAtivos = false;

    /** Hora local (0–23) em que o resumo deve chegar. */
    @Column(name = "hora_lembrete", nullable = false)
    @Builder.Default
    private Integer horaLembrete = 8;

    /** Data local do último envio, para não repetir no mesmo dia. */
    @Column(name = "ultimo_lembrete_em")
    private LocalDate ultimoLembreteEm;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
    }
}
