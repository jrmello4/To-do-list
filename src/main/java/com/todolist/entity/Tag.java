package com.todolist.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tags", uniqueConstraints = {
    @UniqueConstraint(name = "uk_tags_user_nome", columnNames = {"user_id", "nome"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String nome;

    @Column(nullable = false, length = 10)
    @Builder.Default
    private String cor = "#6366f1";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User usuario;

    @ManyToMany(mappedBy = "tags")
    @Builder.Default
    @JsonIgnore
    private List<Task> tasks = new ArrayList<>();
}
