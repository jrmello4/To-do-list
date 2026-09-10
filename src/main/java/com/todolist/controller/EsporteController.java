package com.todolist.controller;

import com.todolist.dto.EsporteEventoResponse;
import com.todolist.dto.EsporteJogoResponse;
import com.todolist.dto.PreferenciaEsporteRequest;
import com.todolist.entity.PreferenciaEsporte;
import com.todolist.service.EsporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/esportes")
@RequiredArgsConstructor
@Tag(name = "Esportes & Calendário", description = "Radar esportivo multi-esportes com personalização e sincronização com a agenda")
public class EsporteController {

    private final EsporteService esporteService;

    @GetMapping("/eventos")
    @Operation(summary = "Obter catálogo de eventos esportivos com filtro de modalidade e busca")
    public ResponseEntity<List<EsporteEventoResponse>> obterEventos(
            @RequestParam(required = false) String esporte,
            @RequestParam(required = false) String busca) {
        return ResponseEntity.ok(esporteService.obterEventos(esporte, busca));
    }

    @GetMapping("/preferencias")
    @Operation(summary = "Obter modalidades e times favoritos seguidos pelo usuário")
    public ResponseEntity<List<PreferenciaEsporte>> obterPreferencias() {
        return ResponseEntity.ok(esporteService.obterPreferenciasDoUsuario());
    }

    @PostMapping("/preferencias")
    @Operation(summary = "Adicionar um esporte, time ou liga de interesse")
    public ResponseEntity<PreferenciaEsporte> adicionarPreferencia(@Valid @RequestBody PreferenciaEsporteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(esporteService.adicionarPreferencia(request));
    }

    @DeleteMapping("/preferencias/{id}")
    @Operation(summary = "Remover um esporte ou time dos interesses")
    public ResponseEntity<Void> removerPreferencia(@PathVariable Long id) {
        esporteService.removerPreferencia(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/eventos/{eventoId}/salvar-calendario")
    @Operation(summary = "Salvar evento esportivo no Calendário Unificado do usuário")
    public ResponseEntity<EsporteEventoResponse> salvarNoCalendario(@PathVariable String eventoId) {
        return ResponseEntity.ok(esporteService.salvarNoCalendario(eventoId));
    }

    @DeleteMapping("/eventos/{eventoId}/remover-calendario")
    @Operation(summary = "Remover evento esportivo do Calendário Unificado do usuário")
    public ResponseEntity<EsporteEventoResponse> removerDoCalendario(@PathVariable String eventoId) {
        return ResponseEntity.ok(esporteService.removerDoCalendario(eventoId));
    }

    @GetMapping("/jogos")
    @Operation(summary = "Endpoint legado para partidas de futebol")
    public ResponseEntity<List<EsporteJogoResponse>> obterJogos(@RequestParam(required = false) String time) {
        return ResponseEntity.ok(esporteService.obterJogosDoDia(time));
    }

    @GetMapping("/alertas")
    @Operation(summary = "Alertas de jogos nas próximas 1h (eventos de Esportes do calendário)")
    public ResponseEntity<List<com.todolist.dto.AlertaEsporteResponse>> alertas() {
        return ResponseEntity.ok(esporteService.coletarAlertasProximos());
    }
}
