package com.todolist.repository;

import com.todolist.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    List<Attachment> findByTaskId(Long taskId);

    Optional<Attachment> findByIdAndTaskUsuarioId(Long id, Long usuarioId);

    Optional<Attachment> findByIdAndTaskIdAndTaskUsuarioId(Long id, Long taskId, Long usuarioId);
}
