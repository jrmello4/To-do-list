package com.todolist.controller;

import com.todolist.dto.AuthResponse;
import com.todolist.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Auto-login exclusivo do modo desktop. Só existe quando o perfil "desktop"
 * está ativo e o servidor escuta em loopback, então não é exposto na web.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Profile("desktop")
@Tag(name = "Autenticação Desktop", description = "Auto-login local do aplicativo desktop")
public class DesktopAuthController {

    private final AuthService authService;

    @PostMapping("/desktop")
    @Operation(summary = "Entrar automaticamente como o proprietário local (modo desktop)")
    public ResponseEntity<AuthResponse> loginDesktop() {
        return ResponseEntity.ok(authService.autenticarDesktop());
    }
}
