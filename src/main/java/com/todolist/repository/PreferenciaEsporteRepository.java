package com.todolist.repository;

import com.todolist.entity.PreferenciaEsporte;
import com.todolist.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PreferenciaEsporteRepository extends JpaRepository<PreferenciaEsporte, Long> {
    List<PreferenciaEsporte> findByUsuarioAndAtivoTrue(User usuario);
    Optional<PreferenciaEsporte> findByIdAndUsuarioAndAtivoTrue(Long id, User usuario);
    long countByUsuarioAndAtivoTrue(User usuario);
}
