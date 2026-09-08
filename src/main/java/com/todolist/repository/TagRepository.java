package com.todolist.repository;

import com.todolist.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    List<Tag> findByUsuarioIdOrderByNomeAsc(Long usuarioId);

    Optional<Tag> findByIdAndUsuarioId(Long id, Long usuarioId);

    boolean existsByUsuarioIdAndNomeIgnoreCase(Long usuarioId, String nome);

    Optional<Tag> findByUsuarioIdAndNomeIgnoreCase(Long usuarioId, String nome);

    List<Tag> findByIdInAndUsuarioId(Collection<Long> ids, Long usuarioId);
}
