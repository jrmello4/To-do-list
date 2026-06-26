package com.todolist.service;

import com.todolist.dto.TaskRequest;
import com.todolist.dto.TaskResponse;
import com.todolist.entity.Task;
import com.todolist.exception.ResourceNotFoundException;
import com.todolist.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskResponse criar(TaskRequest request) {
        Task task = Task.builder()
                .titulo(request.getTitulo())
                .descricao(request.getDescricao())
                .concluida(request.getConcluida() != null && request.getConcluida())
                .build();

        return toResponse(taskRepository.save(task));
    }

    public List<TaskResponse> listarTodas() {
        return taskRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public TaskResponse buscarPorId(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa", id));
        return toResponse(task);
    }

    public TaskResponse atualizar(Long id, TaskRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa", id));

        task.setTitulo(request.getTitulo());
        task.setDescricao(request.getDescricao());

        if (request.getConcluida() != null) {
            task.setConcluida(request.getConcluida());
        }

        return toResponse(taskRepository.save(task));
    }

    public void deletar(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tarefa", id);
        }
        taskRepository.deleteById(id);
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
