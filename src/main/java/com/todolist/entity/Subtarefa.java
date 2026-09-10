package com.todolist.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Um passo dentro de uma tarefa.
 *
 * Não tem dono próprio: o dono é o da tarefa. É de propósito — sem coluna
 * usuario_id aqui, não existe caminho para chegar a um passo sem passar pela
 * tarefa, e a tarefa só é encontrada com o id do dono junto.
 */
@Entity
@Table(name = "subtarefas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subtarefa {

    /**
     * Teto de passos por tarefa. Mora aqui, e não em quem escreve, porque é
     * regra do modelo: a API e a importação precisam concordar, senão um
     * arquivo cria uma tarefa que a própria API teria recusado.
     */
    public static final int LIMITE_POR_TAREFA = 50;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(nullable = false)
    @Builder.Default
    private Boolean concluida = false;

    @Column(nullable = false)
    @Builder.Default
    private Integer ordem = 0;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now(ZoneOffset.UTC);
    }
}
