package com.todolist.repository;

import com.todolist.entity.ContaFinanceira;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContaFinanceiraRepository extends JpaRepository<ContaFinanceira, Long> {
    List<ContaFinanceira> findByUsuarioIdAndAtivoTrueOrderByNomeAsc(Long usuarioId);
    Optional<ContaFinanceira> findByIdAndUsuarioId(Long id, Long usuarioId);
    boolean existsByIdAndUsuarioId(Long id, Long usuarioId);
}