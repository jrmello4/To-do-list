package com.todolist.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final int MAX_ENTRIES = 10_000;
    private static final long STALE_MILLIS = Duration.ofMinutes(10).toMillis();

    private final Map<String, ClientBucket> buckets = new ConcurrentHashMap<>();

    /** Só confie no X-Forwarded-For quando a aplicação estiver atrás de um proxy conhecido. */
    @Value("${app.security.trust-forwarded-for:false}")
    private boolean trustForwardedFor;

    private record ClientBucket(Bucket bucket, long lastAccess) {
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // Rate limit em requisições sensíveis de autenticação (POST)
        if ("POST".equalsIgnoreCase(method)
                && (path.startsWith("/api/auth/login")
                    || path.startsWith("/api/auth/cadastro")
                    || path.startsWith("/api/auth/convidado"))) {
            String clientIp = obterIpCliente(request);
            Bucket bucket = obterBucket(clientIp);

            if (!bucket.tryConsume(1)) {
                response.setStatus(429);
                response.setContentType("application/json; charset=utf-8");
                response.getWriter().write("{\"status\":429,\"mensagem\":\"Limite de requisições excedido. Por favor, aguarde 1 minuto antes de tentar novamente.\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private Bucket obterBucket(String clientIp) {
        long agora = System.currentTimeMillis();
        ClientBucket cb = buckets.compute(clientIp, (key, atual) -> {
            Bucket bucket = (atual != null) ? atual.bucket() : criarNovoBucket();
            return new ClientBucket(bucket, agora);
        });
        if (buckets.size() > MAX_ENTRIES) {
            limparBucketsAntigos(agora);
        }
        return cb.bucket();
    }

    private Bucket criarNovoBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(10)
                .refillGreedy(10, Duration.ofMinutes(1))
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    private void limparBucketsAntigos(long agora) {
        Iterator<Map.Entry<String, ClientBucket>> it = buckets.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, ClientBucket> entry = it.next();
            if (agora - entry.getValue().lastAccess() > STALE_MILLIS) {
                it.remove();
            }
        }
        // Se ainda estiver acima do limite, evita crescimento ilimitado.
        if (buckets.size() > MAX_ENTRIES) {
            buckets.clear();
        }
    }

    private String obterIpCliente(HttpServletRequest request) {
        if (trustForwardedFor) {
            String xfHeader = request.getHeader("X-Forwarded-For");
            if (xfHeader != null && !xfHeader.isEmpty() && !"unknown".equalsIgnoreCase(xfHeader)) {
                return xfHeader.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }
}
