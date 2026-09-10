package com.todolist.repository;

import com.todolist.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    /**
     * Contas candidatas a receber o resumo diário.
     *
     * A varredura roda de hora em hora e antes fazia findAll(), trazendo toda
     * a base para descartar quase tudo em Java. Quem tem lembrete ligado é uma
     * fração das contas, e é o banco quem deve saber disso — o índice
     * idx_usuarios_lembretes existe para esta consulta.
     *
     * A hora não entra no filtro de propósito: ela é comparada no fuso de cada
     * conta, e converter fuso dentro da consulta teria sintaxe própria em cada
     * banco. O que sobra depois deste filtro já é pequeno.
     */
    List<Usuario> findByLembretesAtivosTrueAndAtivoTrue();
}
