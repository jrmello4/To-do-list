package com.todolist.repository;

import com.todolist.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Toda consulta recebe o id do dono.
 *
 * Não existe aqui um findById(Long) de conveniência, e isso é deliberado:
 * autenticar não é autorizar. Uma busca só por id permitiria a um usuário
 * logado ler ou apagar a tarefa de outro trocando o número na URL — o token
 * seria válido e o filtro deixaria passar (falha conhecida como IDOR).
 *
 * O mesmo vale para as consultas por Specification: quem monta o filtro parte
 * sempre de TaskSpecifications.doUsuario.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    List<Task> findByUsuarioIdOrderByIdAsc(Long usuarioId);

    /** A ordem que a conta arrumou, que é a que a exportação deve preservar. */
    List<Task> findByUsuarioIdOrderByOrdemAscIdAsc(Long usuarioId);

    @Query("SELECT COALESCE(MAX(t.ordem), -1) FROM Task t WHERE t.usuario.id = :usuarioId")
    int maiorOrdem(@Param("usuarioId") Long usuarioId);

    /**
     * Abre espaço em uma posição, empurrando para baixo tudo o que já estava
     * dali para a frente.
     *
     * Um único UPDATE, e não uma reescrita de todas as posições: mexer numa
     * tarefa não deveria custar uma linha alterada por tarefa da conta. O
     * preço é que as posições ficam esparsas com o tempo — o que não importa,
     * porque o que a listagem usa é a ordem relativa, nunca o valor.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Task t SET t.ordem = t.ordem + 1 "
            + "WHERE t.usuario.id = :usuarioId AND t.ordem >= :apartirDe")
    void abrirEspaco(@Param("usuarioId") Long usuarioId, @Param("apartirDe") int apartirDe);

    Optional<Task> findByIdAndUsuarioId(Long id, Long usuarioId);

    boolean existsByIdAndUsuarioId(Long id, Long usuarioId);

    List<Task> findByUsuarioIdAndProjetoId(Long usuarioId, Long projetoId);

    List<Task> findByUsuarioIdAndEtiquetasId(Long usuarioId, Long etiquetaId);

    long countByUsuarioId(Long usuarioId);

    long countByUsuarioIdAndConcluidaTrue(Long usuarioId);

    long countByUsuarioIdAndConcluidaFalseAndPrazoBefore(Long usuarioId, LocalDate data);

    long countByUsuarioIdAndConcluidaFalseAndPrazo(Long usuarioId, LocalDate data);

    /**
     * Concluídas por dia. Agrupado com year/month/day em vez de um CAST para
     * date: são funções que o Hibernate traduz para qualquer banco, e o CAST
     * teria sintaxe diferente em H2 e MySQL.
     */
    @Query("""
            SELECT YEAR(t.dataConclusao), MONTH(t.dataConclusao), DAY(t.dataConclusao), COUNT(t)
            FROM Task t
            WHERE t.usuario.id = :usuarioId
              AND t.concluida = true
              AND t.dataConclusao >= :desde
            GROUP BY YEAR(t.dataConclusao), MONTH(t.dataConclusao), DAY(t.dataConclusao)
            """)
    List<Object[]> contarConcluidasPorDia(@Param("usuarioId") Long usuarioId,
                                          @Param("desde") LocalDateTime desde);

    @Query("""
            SELECT t.prioridade, COUNT(t)
            FROM Task t
            WHERE t.usuario.id = :usuarioId AND t.concluida = false
            GROUP BY t.prioridade
            """)
    List<Object[]> contarPendentesPorPrioridade(@Param("usuarioId") Long usuarioId);

    /**
     * Pares (criação, conclusão) das últimas concluídas. A média é calculada
     * em Java: diferença entre instantes tem sintaxe própria em cada banco, e
     * fazer isso portátil em HQL custa mais do que ganha.
     */
    @Query("""
            SELECT t.dataCriacao, t.dataConclusao
            FROM Task t
            WHERE t.usuario.id = :usuarioId
              AND t.concluida = true
              AND t.dataConclusao IS NOT NULL
            ORDER BY t.dataConclusao DESC
            """)
    List<Object[]> buscarTemposDeConclusao(@Param("usuarioId") Long usuarioId,
                                           org.springframework.data.domain.Pageable limite);
}
