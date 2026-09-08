package com.todolist.repository;

import com.todolist.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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

    Optional<Task> findByIdAndUsuarioId(Long id, Long usuarioId);

    boolean existsByIdAndUsuarioId(Long id, Long usuarioId);

    List<Task> findByUsuarioIdAndProjetoId(Long usuarioId, Long projetoId);

    long countByUsuarioId(Long usuarioId);

    long countByUsuarioIdAndConcluidaTrue(Long usuarioId);

    long countByUsuarioIdAndConcluidaFalseAndPrazoBefore(Long usuarioId, LocalDate data);

    long countByUsuarioIdAndConcluidaFalseAndPrazo(Long usuarioId, LocalDate data);
}
