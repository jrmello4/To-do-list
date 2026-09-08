package com.todolist.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class RateLimitingFilterTest {

    private RateLimitingFilter rateLimitingFilter;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        rateLimitingFilter = new RateLimitingFilter();
        filterChain = mock(FilterChain.class);
    }

    @Test
    @DisplayName("Deve permitir requisições normais até o limite de 10")
    void devePermitirAteOLimite() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        request.setRemoteAddr("192.168.1.100");

        for (int i = 0; i < 10; i++) {
            MockHttpServletResponse response = new MockHttpServletResponse();
            rateLimitingFilter.doFilter(request, response, filterChain);
            assertThat(response.getStatus()).isEqualTo(200);
        }

        verify(filterChain, times(10)).doFilter(any(), any());
    }

    @Test
    @DisplayName("Deve bloquear com 429 quando exceder 10 requisições por minuto")
    void deveBloquearAposExcederLimite() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        request.setRemoteAddr("192.168.1.200");

        // 10 requisições permitidas
        for (int i = 0; i < 10; i++) {
            MockHttpServletResponse response = new MockHttpServletResponse();
            rateLimitingFilter.doFilter(request, response, filterChain);
        }

        // 11ª requisição deve ser bloqueada
        MockHttpServletResponse blockedResponse = new MockHttpServletResponse();
        rateLimitingFilter.doFilter(request, blockedResponse, filterChain);

        assertThat(blockedResponse.getStatus()).isEqualTo(429);
        assertThat(blockedResponse.getContentAsString()).contains("Limite de requisições excedido");
    }

    @Test
    @DisplayName("Não deve aplicar rate limit em rotas não-sensíveis (ex: GET /api/tarefas)")
    void naoDeveLimitarRotasNormais() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/tarefas");
        request.setRemoteAddr("192.168.1.300");

        for (int i = 0; i < 15; i++) {
            MockHttpServletResponse response = new MockHttpServletResponse();
            rateLimitingFilter.doFilter(request, response, filterChain);
            assertThat(response.getStatus()).isEqualTo(200);
        }

        verify(filterChain, times(15)).doFilter(any(), any());
    }
}
