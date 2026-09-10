package com.todolist.repository;

import com.todolist.entity.RegistroHabito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RegistroHabitoRepository extends JpaRepository<RegistroHabito, Long> {
    Optional<RegistroHabito> findByHabitoIdAndDataRegistro(Long habitoId, LocalDate dataRegistro);

    List<RegistroHabito> findByHabitoIdOrderByDataRegistroDesc(Long habitoId);

    List<RegistroHabito> findByHabitoIdAndDataRegistroGreaterThanEqualAndConcluidoTrue(
            Long habitoId, LocalDate dataInicio);

    void deleteByHabitoIdAndDataRegistro(Long habitoId, LocalDate dataRegistro);
}
