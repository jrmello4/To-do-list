package com.todolist.repository;

import com.todolist.entity.Categoria;
import com.todolist.entity.Prioridade;
import com.todolist.entity.StatusTarefa;
import com.todolist.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByUsuarioIdAndDeletadaFalseOrderByDataCriacaoDesc(Long usuarioId);

    List<Task> findByUsuarioIdAndDeletadaFalseAndDataVencimentoBetween(Long usuarioId, LocalDate inicio, LocalDate fim);

    List<Task> findByUsuarioIdAndDeletadaTrueOrderByDataDelecaoDesc(Long usuarioId);

    Optional<Task> findByIdAndUsuarioIdAndDeletadaFalse(Long id, Long usuarioId);

    Optional<Task> findByIdAndUsuarioId(Long id, Long usuarioId);

    boolean existsByIdAndUsuarioId(Long id, Long usuarioId);

    long countByUsuarioIdAndDeletadaFalse(Long usuarioId);

    long countByUsuarioIdAndDeletadaFalseAndConcluida(Long usuarioId, Boolean concluida);

    long countByUsuarioIdAndDeletadaFalseAndStatus(Long usuarioId, StatusTarefa status);

    long countByUsuarioIdAndDeletadaTrue(Long usuarioId);

    long countByUsuarioIdAndDeletadaFalseAndConcluidaFalseAndDataVencimentoBefore(Long usuarioId, LocalDate data);

    long countByDeletadaFalseAndRecorrenciaNot(com.todolist.entity.Recorrencia recorrencia);

    long countByUsuarioIdAndDeletadaFalseAndRecorrenciaNot(Long usuarioId, com.todolist.entity.Recorrencia recorrencia);

    @Query("SELECT DISTINCT t FROM Task t LEFT JOIN FETCH t.subtarefas WHERE " +
           "t.usuario.id = :usuarioId AND " +
           "t.deletada = false AND " +
           "(:concluida IS NULL OR t.concluida = :concluida) AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:prioridade IS NULL OR t.prioridade = :prioridade) AND " +
           "(:categoria IS NULL OR t.categoria = :categoria) AND " +
           "(:termo IS NULL OR LOWER(t.titulo) LIKE LOWER(CONCAT('%', :termo, '%')) " +
           "OR (t.descricao IS NOT NULL AND LOWER(t.descricao) LIKE LOWER(CONCAT('%', :termo, '%')))) " +
           "ORDER BY t.dataCriacao DESC")
    List<Task> findByUsuarioFiltrosAvancados(
            @Param("usuarioId") Long usuarioId,
            @Param("concluida") Boolean concluida,
            @Param("status") StatusTarefa status,
            @Param("prioridade") Prioridade prioridade,
            @Param("categoria") Categoria categoria,
            @Param("termo") String termo);

    @Modifying
    @Query("DELETE FROM Task t WHERE t.usuario.id = :usuarioId AND t.deletada = true")
    void esvaziarLixeiraDoUsuario(@Param("usuarioId") Long usuarioId);
}
