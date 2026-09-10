package com.todolist.controller;

import com.todolist.dto.*;
import com.todolist.exception.TentativasExcedidasException;
import com.todolist.security.ControleDeTentativas;
import com.todolist.security.UsuarioAutenticado;
import com.todolist.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Cadastro, login e perfil da conta")
public class AuthController {

    private static final String ERRO_JSON = "application/json";

    private final AuthService authService;
    private final ControleDeTentativas tentativas;

    @PostMapping("/registrar")
    @SecurityRequirements
    @Operation(
            summary = "Criar uma conta",
            description = "Cadastra um usuário e já devolve o token, para não exigir um login "
                    + "logo em seguida. O e-mail é normalizado para minúsculas."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Conta criada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(mediaType = ERRO_JSON,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Já existe conta com esse e-mail",
                    content = @Content(mediaType = ERRO_JSON,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AuthResponse> registrar(@Valid @RequestBody RegistroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(request));
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(
            summary = "Entrar",
            description = "Autentica e devolve o token. Envie-o nas demais rotas no cabeçalho "
                    + "Authorization, no formato \"Bearer <token>\"."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(mediaType = ERRO_JSON,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "E-mail ou senha incorretos",
                    content = @Content(mediaType = ERRO_JSON,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "429", description = "Tentativas demais da mesma origem",
                    content = @Content(mediaType = ERRO_JSON,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
                                              HttpServletRequest requisicao) {
        // A conferência vem antes de chamar o serviço de propósito: o custo de
        // uma tentativa é o hash do BCrypt, e recusar depois de pagá-lo não
        // protegeria a CPU, que é metade do problema.
        String origem = requisicao.getRemoteAddr();

        if (tentativas.bloqueado(origem, request.getEmail())) {
            throw new TentativasExcedidasException(tentativas.janela());
        }

        try {
            AuthResponse resposta = authService.login(request);
            tentativas.registrarAcerto(origem, request.getEmail());
            return ResponseEntity.ok(resposta);
        } catch (AuthenticationException e) {
            tentativas.registrarFalha(origem, request.getEmail());
            throw e;
        }
    }

    @GetMapping("/eu")
    @Operation(summary = "Perfil da conta autenticada")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil retornado"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(mediaType = ERRO_JSON,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UsuarioResponse> eu(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(authService.perfil(usuario.getId()));
    }

    @PutMapping("/senha")
    @Operation(
            summary = "Trocar a senha",
            description = "Exige a senha atual — um token roubado não deve bastar para tomar a "
                    + "conta. Todo token emitido antes para de valer, inclusive o que fez esta "
                    + "chamada; a resposta já traz um novo no lugar."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Senha trocada, com token novo"),
            @ApiResponse(responseCode = "400", description = "Nova senha inválida",
                    content = @Content(mediaType = ERRO_JSON,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Senha atual incorreta",
                    content = @Content(mediaType = ERRO_JSON,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AuthResponse> trocarSenha(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Valid @RequestBody TrocaDeSenhaRequest request) {
        return ResponseEntity.ok(authService.trocarSenha(usuario.getId(), request));
    }

    @PostMapping("/sair-de-todos")
    @Operation(
            summary = "Sair de todos os aparelhos",
            description = "Invalida todos os tokens da conta, inclusive o desta chamada. Existe "
                    + "porque apagar o token do navegador não impede quem já tenha uma cópia "
                    + "dele: sem sessão no servidor, é o único jeito de cortar um token vazado "
                    + "antes de ele expirar."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Tokens invalidados"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou inválido",
                    content = @Content(mediaType = ERRO_JSON,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> sairDeTodos(
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        authService.sairDeTodosOsAparelhos(usuario.getId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/preferencias")
    @Operation(
            summary = "Salvar preferências de lembrete",
            description = "O resumo diário chega na hora local configurada. A interface envia o "
                    + "fuso do próprio navegador, para o lembrete das 8 ser 8 de quem lê."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Preferências salvas"),
            @ApiResponse(responseCode = "400", description = "Hora ou fuso inválidos",
                    content = @Content(mediaType = ERRO_JSON,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UsuarioResponse> preferencias(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Valid @RequestBody PreferenciasRequest request) {
        return ResponseEntity.ok(authService.salvarPreferencias(usuario.getId(), request));
    }
}
