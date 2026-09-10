package com.todolist.controller;

import com.todolist.dto.EventoCalendarioRequest;
import com.todolist.dto.ItemCalendarioResponse;
import com.todolist.service.CalendarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/calendario")
@RequiredArgsConstructor
@Tag(name = "Calendário Unificado", description = "Endpoints para consolidação de tarefas, contas a pagar/receber e eventos")
public class CalendarioController {

    private final CalendarioService calendarioService;

    @GetMapping
    @Operation(summary = "Obter itens unificados do calendário por mês")
    public ResponseEntity<List<ItemCalendarioResponse>> obterItens(
            @RequestParam(required = false) Integer ano,
            @RequestParam(required = false) Integer mes) {
        LocalDate now = LocalDate.now();
        int anoFinal = ano != null ? ano : now.getYear();
        int mesFinal = mes != null ? mes : now.getMonthValue();

        return ResponseEntity.ok(calendarioService.obterItensDoMes(anoFinal, mesFinal));
    }

    @PostMapping("/eventos")
    @Operation(summary = "Criar um novo evento pessoal no calendário")
    public ResponseEntity<ItemCalendarioResponse> criarEvento(@Valid @RequestBody EventoCalendarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(calendarioService.criarEvento(request));
    }

    @DeleteMapping("/eventos/{id}")
    @Operation(summary = "Excluir um evento do calendário")
    public ResponseEntity<Void> excluirEvento(@PathVariable Long id) {
        calendarioService.excluirEvento(id);
        return ResponseEntity.noContent().build();
    }
}
