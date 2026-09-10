package com.todolist.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.todolist.dto.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Recusa corpos grandes demais antes de alguém tentar desserializá-los.
 *
 * A importação já tinha um teto — 5000 itens por tipo —, mas conferido depois
 * de o Jackson montar a lista inteira em memória. Um POST de algumas centenas
 * de megabytes derrubava a aplicação sem nunca chegar na conferência: o limite
 * de negócio não protege contra o custo de ler o pedido.
 *
 * Os dois casos precisam ser tratados:
 *
 * - Content-Length declarado maior que o teto: recusa na hora, sem ler nada.
 * - Sem Content-Length (Transfer-Encoding: chunked), que é como se contorna a
 *   conferência acima: o corpo é lido por um fluxo que para no teto. Confiar
 *   só no cabeçalho seria confiar em quem envia.
 */
@Component
// Primeiro da fila: a conferência do Content-Length não custa nada e evita
// que qualquer coisa depois dela toque num corpo grande demais.
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class LimiteDeCorpoFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    /**
     * Teto do corpo, em bytes. Uma exportação de conta cheia — 5000 tarefas com
     * passos, projetos e etiquetas — fica na casa de poucos megabytes, então
     * 10 MB aceita o arquivo legítimo com folga larga.
     */
    @Value("${LIMITE_CORPO_BYTES:10485760}")
    private long limiteBytes;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest requisicao) {
        // Só o que tem corpo. GET e DELETE não passam por aqui.
        String metodo = requisicao.getMethod();
        return !("POST".equals(metodo) || "PUT".equals(metodo) || "PATCH".equals(metodo));
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest requisicao,
                                    @NonNull HttpServletResponse resposta,
                                    @NonNull FilterChain cadeia)
            throws ServletException, IOException {

        if (requisicao.getContentLengthLong() > limiteBytes) {
            recusar(resposta);
            return;
        }

        try {
            cadeia.doFilter(new RequisicaoLimitada(requisicao, limiteBytes), resposta);
        } catch (CorpoGrandeDemaisException e) {
            // Chegou aqui embrulhada pelo conversor de mensagem do Spring: o
            // estouro acontece no meio da desserialização.
            if (!resposta.isCommitted()) {
                recusar(resposta);
            }
        }
    }

    private void recusar(HttpServletResponse resposta) throws IOException {
        resposta.setStatus(HttpStatus.PAYLOAD_TOO_LARGE.value());
        resposta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        resposta.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(resposta.getOutputStream(), ErrorResponse.of(
                HttpStatus.PAYLOAD_TOO_LARGE.value(),
                "Corpo da requisição grande demais (limite de "
                        + limiteBytes / (1024 * 1024) + " MB)"));
    }

    /** Sinaliza o estouro de dentro da leitura, onde não há resposta na mão. */
    static class CorpoGrandeDemaisException extends RuntimeException {
        CorpoGrandeDemaisException() {
            super("Corpo da requisição acima do limite");
        }
    }

    private static final class RequisicaoLimitada extends HttpServletRequestWrapper {

        private final long limite;

        RequisicaoLimitada(HttpServletRequest requisicao, long limite) {
            super(requisicao);
            this.limite = limite;
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            ServletInputStream original = super.getInputStream();

            return new ServletInputStream() {
                private long lidos;

                private int contar(int quantidade) {
                    if (quantidade > 0) {
                        lidos += quantidade;
                        if (lidos > limite) {
                            throw new CorpoGrandeDemaisException();
                        }
                    }
                    return quantidade;
                }

                @Override
                public int read() throws IOException {
                    int b = original.read();
                    contar(b == -1 ? 0 : 1);
                    return b;
                }

                @Override
                public int read(@NonNull byte[] destino, int inicio, int tamanho)
                        throws IOException {
                    return contar(original.read(destino, inicio, tamanho));
                }

                @Override
                public boolean isFinished() {
                    return original.isFinished();
                }

                @Override
                public boolean isReady() {
                    return original.isReady();
                }

                @Override
                public void setReadListener(ReadListener ouvinte) {
                    original.setReadListener(ouvinte);
                }
            };
        }
    }
}
