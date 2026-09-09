package com.todolist.controller;

import com.todolist.dto.PainelResponse;
import com.todolist.security.UsuarioAutenticado;
import com.todolist.service.PainelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/painel")
@RequiredArgsConstructor
@Tag(name = "Painel", description = "Números agregados da conta")
public class PainelController {

    private final PainelService painelService;

    @GetMapping
    @Operation(
            summary = "Resumo geral da conta",
            description = """
                    Tudo agregado no banco, em vez de mandar as tarefas todas e somar no
                    navegador — o que funciona com 50 tarefas e derrete com 5 mil.

                    A série de conclusões vem contínua: dias sem conclusão aparecem com zero,
                    para o gráfico não comprimir os intervalos vazios.
                    """
    )
    public ResponseEntity<PainelResponse> painel(
            @AuthenticationPrincipal UsuarioAutenticado usuario,

            @Parameter(description = "Data de referência. Envie o hoje de quem usa.",
                    example = "2026-09-08")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hoje,

            @Parameter(description = "Tamanho da série de conclusões, em dias (7 a 365)",
                    example = "30")
            @RequestParam(defaultValue = "30") int dias) {

        return ResponseEntity.ok(painelService.montar(usuario.getId(), hoje, dias));
    }
}
