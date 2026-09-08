package com.todolist.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // Rate limit apenas para requisições sensíveis de autenticação (POST)
        if ("POST".equalsIgnoreCase(method) && (path.startsWith("/api/auth/login") || path.startsWith("/api/auth/cadastro"))) {
            String clientIp = obterIpCliente(request);
            Bucket bucket = buckets.computeIfAbsent(clientIp, this::criarNovoBucket);

            if (!bucket.tryConsume(1)) {
                response.setStatus(429);
                response.setContentType("application/json; charset=utf-8");
                response.getWriter().write("{\"status\":429,\"mensagem\":\"Limite de requisições excedido. Por favor, aguarde 1 minuto antes de tentar novamente.\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private Bucket criarNovoBucket(String clientIp) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(10)
                .refillGreedy(10, Duration.ofMinutes(1))
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private String obterIpCliente(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || "unknown".equalsIgnoreCase(xfHeader)) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}
