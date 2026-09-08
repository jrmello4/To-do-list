package com.todolist.scheduler;

import com.todolist.entity.Recorrencia;
import com.todolist.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class RecurringTaskScheduler {

    private final TaskRepository taskRepository;

    /**
     * Varredura diária à 01:00 AM para auditoria e log de tarefas com recorrência ativa.
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void verificarTarefasRecorrentes() {
        log.info("Iniciando auditoria diária de tarefas recorrentes...");
        long totalRecorrentes = taskRepository.countByDeletadaFalseAndRecorrenciaNot(Recorrencia.NENHUMA);
        log.info("Total de tarefas com recorrência ativa no sistema: {}", totalRecorrentes);
    }
}
