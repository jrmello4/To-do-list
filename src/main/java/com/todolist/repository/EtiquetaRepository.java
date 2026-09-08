package com.todolist.repository;

import com.todolist.entity.Etiqueta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Como nos demais repositórios, nenhuma consulta busca só por id. */
@Repository
public interface EtiquetaRepository extends JpaRepository<Etiqueta, Long> {

    List<Etiqueta> findByUsuarioIdOrderByNomeAsc(Long usuarioId);

    Optional<Etiqueta> findByIdAndUsuarioId(Long id, Long usuarioId);

    List<Etiqueta> findByUsuarioIdAndIdIn(Long usuarioId, List<Long> ids);

    boolean existsByUsuarioIdAndNomeIgnoreCase(Long usuarioId, String nome);

    boolean existsByUsuarioIdAndNomeIgnoreCaseAndIdNot(Long usuarioId, String nome, Long id);

    @Query("""
            SELECT e.id, COUNT(t)
            FROM Task t JOIN t.etiquetas e
            WHERE t.usuario.id = :usuarioId AND t.concluida = false
            GROUP BY e.id
            """)
    List<Object[]> contarPendentesPorEtiqueta(@Param("usuarioId") Long usuarioId);
}
