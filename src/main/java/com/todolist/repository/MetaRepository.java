package com.todolist.repository;

import com.todolist.entity.Meta;
import com.todolist.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetaRepository extends JpaRepository<Meta, Long> {
    List<Meta> findByUsuarioAndAtivoTrueOrderByConcluidaAscPrazoAsc(User usuario);
    long countByUsuarioAndAtivoTrue(User usuario);
}
