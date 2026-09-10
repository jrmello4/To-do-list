package com.todolist.controller;

import com.todolist.dto.DashboardResumoResponse;
import com.todolist.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Resumo unificado do Cockpit Diário (Tarefas + Finanças)")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/resumo")
    @Operation(summary = "Obter resumo consolidado do Cockpit Diário para o usuário autenticado")
    public ResponseEntity<DashboardResumoResponse> obterResumo() {
        return ResponseEntity.ok(dashboardService.obterResumo());
    }
}