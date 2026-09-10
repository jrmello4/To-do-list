package com.todolist.repository;

import com.todolist.entity.EventoCalendario;
import com.todolist.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EventoCalendarioRepository extends JpaRepository<EventoCalendario, Long> {
    List<EventoCalendario> findByUsuarioAndAtivoTrueAndDataEventoBetweenOrderByDataEventoAscHoraInicioAsc(
            User usuario, LocalDate inicio, LocalDate fim
    );

    @Query("""
            SELECT e FROM EventoCalendario e
            WHERE e.usuario = :usuario
              AND e.ativo = true
              AND e.alertaEnviado = false
              AND e.categoria = 'Esportes'
              AND e.dataEvento = :hoje
              AND e.horaInicio IS NOT NULL
            ORDER BY e.horaInicio ASC
            """)
    List<EventoCalendario> findEsportesSemAlertaHoje(
            @Param("usuario") User usuario,
            @Param("hoje") LocalDate hoje);
}
