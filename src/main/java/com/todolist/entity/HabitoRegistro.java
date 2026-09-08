package com.todolist.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/** Uma linha por dia cumprido. A ausência da linha é o "não fiz". */
@Entity
@Table(name = "habito_registros")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HabitoRegistro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "habito_id", nullable = false)
    private Habito habito;

    @Column(nullable = false)
    private LocalDate data;
}
