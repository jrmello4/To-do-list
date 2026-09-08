package com.todolist.repository;

import com.todolist.entity.Habito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HabitoRepository extends JpaRepository<Habito, Long> {

    List<Habito> findByUsuarioIdOrderByNomeAsc(Long usuarioId);

    Optional<Habito> findByIdAndUsuarioId(Long id, Long usuarioId);

    boolean existsByUsuarioIdAndNomeIgnoreCase(Long usuarioId, String nome);

    boolean existsByUsuarioIdAndNomeIgnoreCaseAndIdNot(Long usuarioId, String nome, Long id);
}
