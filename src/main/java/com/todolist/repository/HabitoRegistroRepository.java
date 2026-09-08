package com.todolist.repository;

import com.todolist.entity.HabitoRegistro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HabitoRegistroRepository extends JpaRepository<HabitoRegistro, Long> {

    Optional<HabitoRegistro> findByHabitoIdAndData(Long habitoId, LocalDate data);

    /**
     * Apagados explicitamente antes do hábito. O ON DELETE CASCADE resolve no
     * banco, mas não na sessão do Hibernate — a mesma armadilha que apareceu
     * ao excluir etiquetas.
     */
    void deleteByHabitoId(Long habitoId);

    /**
     * Registros de todos os hábitos da conta desde uma data. Numa consulta só:
     * buscar hábito a hábito seria N+1 na tela que mais os exibe junta.
     */
    @Query("""
            SELECT r FROM HabitoRegistro r
            WHERE r.habito.usuario.id = :usuarioId AND r.data >= :desde
            ORDER BY r.data DESC
            """)
    List<HabitoRegistro> buscarDaConta(@Param("usuarioId") Long usuarioId,
                                       @Param("desde") LocalDate desde);
}
