package com.todolist.repository;

import com.todolist.entity.Projeto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Como em TaskRepository, nenhuma consulta busca só por id. */
@Repository
public interface ProjetoRepository extends JpaRepository<Projeto, Long> {

    List<Projeto> findByUsuarioIdOrderByNomeAsc(Long usuarioId);

    Optional<Projeto> findByIdAndUsuarioId(Long id, Long usuarioId);

    boolean existsByUsuarioIdAndNomeIgnoreCase(Long usuarioId, String nome);

    boolean existsByUsuarioIdAndNomeIgnoreCaseAndIdNot(Long usuarioId, String nome, Long id);

    /**
     * Contagem de pendentes por projeto numa consulta só. Contar projeto a
     * projeto seria N+1 — barato com três projetos, caro com trinta.
     */
    @Query("""
            SELECT t.projeto.id, COUNT(t)
            FROM Task t
            WHERE t.usuario.id = :usuarioId
              AND t.concluida = false
              AND t.projeto IS NOT NULL
            GROUP BY t.projeto.id
            """)
    List<Object[]> contarPendentesPorProjeto(@Param("usuarioId") Long usuarioId);
}
