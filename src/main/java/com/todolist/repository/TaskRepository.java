package com.todolist.repository;

import com.todolist.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Toda consulta recebe o id do dono.
 *
 * Não existe aqui um findById(Long) de conveniência, e isso é deliberado:
 * autenticar não é autorizar. Uma busca só por id permitiria a um usuário
 * logado ler ou apagar a tarefa de outro trocando o número na URL — o token
 * seria válido e o filtro deixaria passar (falha conhecida como IDOR).
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByUsuarioIdOrderByIdAsc(Long usuarioId);

    Optional<Task> findByIdAndUsuarioId(Long id, Long usuarioId);

    boolean existsByIdAndUsuarioId(Long id, Long usuarioId);
}
