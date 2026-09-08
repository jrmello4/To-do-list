package com.todolist.service;

import com.todolist.dto.TaskRequest;
import com.todolist.dto.TaskResponse;
import com.todolist.entity.Task;
import com.todolist.exception.ResourceNotFoundException;
import com.todolist.repository.TaskRepository;
import com.todolist.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public TaskResponse criar(Long usuarioId, TaskRequest request) {
        Task task = Task.builder()
                // getReferenceById devolve uma referência preguiçosa: grava a
                // chave estrangeira sem ir ao banco buscar o usuário inteiro.
                .usuario(usuarioRepository.getReferenceById(usuarioId))
                .titulo(request.getTitulo())
                .descricao(request.getDescricao())
                .concluida(request.getConcluida() != null && request.getConcluida())
                .build();

        return toResponse(taskRepository.save(task));
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> listarTodas(Long usuarioId) {
        return taskRepository.findByUsuarioIdOrderByIdAsc(usuarioId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskResponse buscarPorId(Long usuarioId, Long id) {
        Task task = buscarDoUsuario(usuarioId, id);
        return toResponse(task);
    }

    @Transactional
    public TaskResponse atualizar(Long usuarioId, Long id, TaskRequest request) {
        Task task = buscarDoUsuario(usuarioId, id);

        task.setTitulo(request.getTitulo());
        task.setDescricao(request.getDescricao());

        if (request.getConcluida() != null) {
            task.setConcluida(request.getConcluida());
        }

        return toResponse(taskRepository.save(task));
    }

    @Transactional
    public void deletar(Long usuarioId, Long id) {
        taskRepository.delete(buscarDoUsuario(usuarioId, id));
    }

    /**
     * Único caminho para chegar a uma tarefa por id. Tarefa inexistente e
     * tarefa de outro usuário produzem o mesmo 404 de propósito: um 403
     * confirmaria que aquele id existe.
     */
    private Task buscarDoUsuario(Long usuarioId, Long id) {
        return taskRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa", id));
    }

    private TaskResponse toResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .titulo(task.getTitulo())
                .descricao(task.getDescricao())
                .concluida(task.getConcluida())
                .dataCriacao(task.getDataCriacao())
                .dataAtualizacao(task.getDataAtualizacao())
                .build();
    }
}
