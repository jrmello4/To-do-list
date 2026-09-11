package com.todolist.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false)
    @Builder.Default
    private Boolean concluida = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Prioridade prioridade = Prioridade.MEDIA;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatusTarefa status = StatusTarefa.A_FAZER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private Categoria categoria = Categoria.GERAL;

    private LocalDate dataVencimento;

    @Column(nullable = false)
    @Builder.Default
    private Boolean deletada = false;

    private LocalDateTime dataDelecao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Recorrencia recorrencia = Recorrencia.NENHUMA;

    @Column(nullable = false)
    @Builder.Default
    private Integer pomodorosEstimados = 1;

    @Column(nullable = false)
    @Builder.Default
    private Integer pomodorosRealizados = 0;

    @Column(name = "recorrencia_gerada", nullable = false)
    @Builder.Default
    private Boolean recorrenciaGerada = false;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Subtask> subtarefas = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "task_tags",
        joinColumns = @JoinColumn(name = "task_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Attachment> anexos = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User usuario;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    private LocalDateTime dataAtualizacao;

    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
        dataAtualizacao = LocalDateTime.now();
        if (concluida == null) concluida = false;
        if (prioridade == null) prioridade = Prioridade.MEDIA;
        if (status == null) status = Boolean.TRUE.equals(concluida) ? StatusTarefa.CONCLUIDA : StatusTarefa.A_FAZER;
        if (categoria == null) categoria = Categoria.GERAL;
        if (deletada == null) deletada = false;
        if (recorrencia == null) recorrencia = Recorrencia.NENHUMA;
        if (pomodorosEstimados == null) pomodorosEstimados = 1;
        if (pomodorosRealizados == null) pomodorosRealizados = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        dataAtualizacao = LocalDateTime.now();
    }

    public void sincronizarStatusComConcluida(boolean isConcluida) {
        this.concluida = isConcluida;
        if (isConcluida) {
            this.status = StatusTarefa.CONCLUIDA;
        } else if (this.status == StatusTarefa.CONCLUIDA) {
            this.status = StatusTarefa.A_FAZER;
        }
    }

    public void sincronizarStatus(StatusTarefa novoStatus) {
        this.status = novoStatus;
        this.concluida = (novoStatus == StatusTarefa.CONCLUIDA);
    }
}
