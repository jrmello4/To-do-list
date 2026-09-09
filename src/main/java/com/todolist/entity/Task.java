package com.todolist.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
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

    /**
     * Posição na lista da conta. Empates são desfeitos pelo id, então a ordem
     * é sempre total mesmo que dois valores coincidam.
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer ordem = 0;

    /** Quando a tarefa passou a concluída. Base das métricas da fase seguinte. */
    @Column(name = "data_conclusao")
    private LocalDateTime dataConclusao;

    /**
     * Etiquetas da tarefa. Transversais aos projetos: uma tarefa pertence a um
     * projeto só, mas pode ter várias etiquetas.
     *
     * LinkedHashSet e não List: o Set casa com a chave primária composta da
     * tabela de junção, que já impede repetição.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    // Sem o BatchSize, montar uma página de 50 tarefas dispararia 50 consultas
    // para carregar as etiquetas — uma por tarefa. Com ele, viram uma ou duas.
    @BatchSize(size = 50)
    @JoinTable(
            name = "task_etiquetas",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "etiqueta_id"))
    @Builder.Default
    private Set<Etiqueta> etiquetas = new LinkedHashSet<>();

    /**
     * Passos da tarefa, em ordem.
     *
     * List e não Set: aqui a ordem é informação, não acidente — passo 2 vem
     * depois do passo 1. E cascade + orphanRemoval porque a subtarefa não
     * existe fora da tarefa; o ON DELETE CASCADE da migration é a rede de
     * baixo, para quem apagar por SQL direto. Confiar só nele já custou caro
     * neste projeto: o banco apaga a linha, mas a sessão do Hibernate segue
     * com o objeto na mão.
     */
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem ASC, id ASC")
    @BatchSize(size = 50)
    @Builder.Default
    private List<Subtarefa> subtarefas = new ArrayList<>();

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
