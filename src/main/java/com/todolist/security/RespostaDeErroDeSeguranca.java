package com.todolist.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.todolist.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Sem isto o Spring Security responderia com o corpo padrão dele, e a API
 * passaria a ter dois formatos de erro diferentes. Aqui 401 e 403 saem no mesmo
 * ErrorResponse que o GlobalExceptionHandler já usa.
 */
@Component
@RequiredArgsConstructor
public class RespostaDeErroDeSeguranca implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest requisicao, HttpServletResponse resposta,
                         AuthenticationException excecao) throws IOException {
        escrever(resposta, HttpStatus.UNAUTHORIZED,
                "Autenticação necessária. Envie o cabeçalho Authorization: Bearer <token>.");
    }

    @Override
    public void handle(HttpServletRequest requisicao, HttpServletResponse resposta,
                       AccessDeniedException excecao) throws IOException {
        escrever(resposta, HttpStatus.FORBIDDEN, "Acesso negado a este recurso.");
    }

    private void escrever(HttpServletResponse resposta, HttpStatus status, String mensagem)
            throws IOException {
        resposta.setStatus(status.value());
        resposta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        resposta.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(resposta.getOutputStream(),
                ErrorResponse.of(status.value(), mensagem));
    }
}
