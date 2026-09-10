package com.todolist.repository;

import com.todolist.entity.NotaRapida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotaRapidaRepository extends JpaRepository<NotaRapida, Long> {
    List<NotaRapida> findByUsuarioIdOrderByFixadaDescDataAtualizacaoDesc(Long usuarioId);
    Optional<NotaRapida> findByIdAndUsuarioId(Long id, Long usuarioId);
}