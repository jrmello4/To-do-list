package com.todolist.service;

import com.todolist.dto.TagRequest;
import com.todolist.dto.TagResponse;
import com.todolist.entity.Tag;
import com.todolist.entity.User;
import com.todolist.exception.ResourceNotFoundException;
import com.todolist.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final AuthService authService;

    @Transactional(readOnly = true)
    public List<TagResponse> listarPorUsuario() {
        User user = authService.obterUsuarioAutenticado();
        return tagRepository.findByUsuarioIdOrderByNomeAsc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public TagResponse criar(TagRequest request) {
        User user = authService.obterUsuarioAutenticado();
        String nomeLimpo = request.getNome().trim();

        if (tagRepository.existsByUsuarioIdAndNomeIgnoreCase(user.getId(), nomeLimpo)) {
            throw new IllegalArgumentException("Já existe uma tag com este nome.");
        }

        String cor = (request.getCor() != null && !request.getCor().isBlank()) ? request.getCor() : "#6366f1";

        Tag tag = Tag.builder()
                .nome(nomeLimpo)
                .cor(cor)
                .usuario(user)
                .build();

        return toResponse(tagRepository.save(tag));
    }

    @Transactional
    public void deletar(Long id) {
        User user = authService.obterUsuarioAutenticado();
        Tag tag = tagRepository.findByIdAndUsuarioId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Tag", id));

        // Remove a tag de todas as tarefas associadas antes de deletar
        if (tag.getTasks() != null) {
            tag.getTasks().forEach(task -> task.getTags().remove(tag));
        }

        tagRepository.delete(tag);
    }

    public TagResponse toResponse(Tag tag) {
        return TagResponse.builder()
                .id(tag.getId())
                .nome(tag.getNome())
                .cor(tag.getCor())
                .build();
    }
}
