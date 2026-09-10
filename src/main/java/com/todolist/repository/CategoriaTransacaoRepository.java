package com.todolist.repository;

import com.todolist.entity.CategoriaTransacao;
import com.todolist.entity.TipoTransacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaTransacaoRepository extends JpaRepository<CategoriaTransacao, Long> {
    List<CategoriaTransacao> findByUsuarioIdOrderByNomeAsc(Long usuarioId);
    List<CategoriaTransacao> findByUsuarioIdAndTipoOrderByNomeAsc(Long usuarioId, TipoTransacao tipo);
    Optional<CategoriaTransacao> findByIdAndUsuarioId(Long id, Long usuarioId);
    boolean existsByUsuarioIdAndNomeIgnoreCase(Long usuarioId, String nome);
}