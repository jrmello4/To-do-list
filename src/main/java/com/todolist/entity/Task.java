package com.todolist.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Dono da tarefa. LAZY porque nenhuma listagem precisa carregar o usuário —
     * as consultas filtram por usuario_id, sem navegar pelo relacionamento.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    /** Nulo significa caixa de entrada: a tarefa não pertence a projeto algum. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projeto_id")
    private Projeto projeto;

    @Column(nullable = false)
    @Builder.Default
    private Boolean concluida = false;

    /** Data, e não instante: prazo é uma decisão sobre o dia. */
    private LocalDate prazo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private Prioridade prioridade = Prioridade.MEDIA;

    /** Quando a tarefa passou a concluída. Base das métricas da fase seguinte. */
    @Column(name = "data_conclusao")
    private LocalDateTime dataConclusao;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    private LocalDateTime dataAtualizacao;

    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
        dataAtualizacao = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        dataAtualizacao = LocalDateTime.now();
    }
}
