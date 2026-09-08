package com.todolist.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "habitos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Habito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String cor = "verde";

    /** Números ISO separados por vírgula: "1,3,5" é segunda, quarta e sexta. */
    @Column(name = "dias_semana", nullable = false, length = 20)
    @Builder.Default
    private String diasSemana = "1,2,3,4,5,6,7";

    @Column(nullable = false)
    @Builder.Default
    private Boolean ativo = true;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
    }

    /** Conjunto de dias em que o hábito vale, já como DayOfWeek. */
    public Set<DayOfWeek> diasComoConjunto() {
        return Arrays.stream(diasSemana.split(","))
                .map(String::trim)
                .filter(parte -> !parte.isEmpty())
                .map(Integer::parseInt)
                .map(DayOfWeek::of)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
