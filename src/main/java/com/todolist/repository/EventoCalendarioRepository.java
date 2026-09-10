package com.todolist.repository;

import com.todolist.entity.EventoCalendario;
import com.todolist.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EventoCalendarioRepository extends JpaRepository<EventoCalendario, Long> {
    List<EventoCalendario> findByUsuarioAndAtivoTrueAndDataEventoBetweenOrderByDataEventoAscHoraInicioAsc(
            User usuario, LocalDate inicio, LocalDate fim
    );
}
