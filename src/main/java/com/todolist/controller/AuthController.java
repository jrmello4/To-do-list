package com.todolist.controller;

import com.todolist.dto.AuthResponse;
import com.todolist.dto.LoginRequest;
import com.todolist.dto.RegisterRequest;
import com.todolist.dto.UserResponse;
import com.todolist.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoints para cadastro, login e perfil do usuário")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/cadastro")
    @Operation(summary = "Cadastrar um novo usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário cadastrado com sucesso e token gerado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou e-mail já cadastrado")
    })
    public ResponseEntity<AuthResponse> cadastrar(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.cadastrar(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuário e obter token JWT")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticação bem-sucedida"),
            @ApiResponse(responseCode = "400", description = "Credenciais inválidas")
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.autenticar(request));
    }

    @PostMapping("/convidado")
    @Operation(summary = "Entrar como convidado (Modo Demonstração/Teste)",
               description = "Gera um token de acesso imediato para testes em desenvolvimento, com tarefas e dados pré-carregados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticação como convidado realizada com sucesso")
    })
    public ResponseEntity<AuthResponse> loginComoConvidado() {
        return ResponseEntity.ok(authService.autenticarComoConvidado());
    }

    @GetMapping("/me")
    @Operation(summary = "Obter dados do usuário autenticado atual")
    public ResponseEntity<UserResponse> obterPerfil() {
        return ResponseEntity.ok(authService.obterPerfilAtual());
    }
}
