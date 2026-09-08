package com.todolist.repository;

import com.todolist.entity.Subtask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubtaskRepository extends JpaRepository<Subtask, Long> {

    Optional<Subtask> findByIdAndTaskId(Long id, Long taskId);
}
