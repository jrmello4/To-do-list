package com.todolist.security;

import com.todolist.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    /** HS256 exige chave de no mínimo 256 bits. */
    private static final int MINIMO_DE_BYTES = 32;

    private final SecretKey chave;
    private final long validadeSegundos;

    public JwtService(@Value("${JWT_SECRET:}") String segredo,
                      @Value("${JWT_EXPIRACAO_SEGUNDOS:86400}") long validadeSegundos) {

        this.validadeSegundos = validadeSegundos;

        if (segredo == null || segredo.isBlank()) {
            // Sem segredo configurado, gera um aleatório em vez de cair num valor
            // padrão embutido no código — que estaria no repositório, público, e
            // permitiria a qualquer um forjar tokens de uma instalação alheia.
            this.chave = Jwts.SIG.HS256.key().build();
            log.warn("JWT_SECRET não definida: chave aleatória gerada para esta execução. "
                    + "Os tokens perdem a validade a cada reinício. Defina JWT_SECRET com pelo "
                    + "menos {} caracteres em qualquer ambiente que não seja desenvolvimento.",
                    MINIMO_DE_BYTES);
            return;
        }

        byte[] bytes = segredo.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < MINIMO_DE_BYTES) {
            throw new IllegalStateException(
                    "JWT_SECRET precisa ter ao menos " + MINIMO_DE_BYTES
                            + " caracteres; recebeu " + bytes.length + ".");
        }

        this.chave = Keys.hmacShaKeyFor(bytes);
    }

    public String gerar(Usuario usuario) {
        Instant agora = Instant.now();

        return Jwts.builder()
                .subject(String.valueOf(usuario.getId()))
                .claim("email", usuario.getEmail())
                .issuedAt(Date.from(agora))
                .expiration(Date.from(agora.plusSeconds(validadeSegundos)))
                .signWith(chave)
                .compact();
    }

    /**
     * Devolve o id do usuário quando o token é válido, e vazio quando não é —
     * expirado, adulterado, assinado com outra chave ou malformado. Quem chama
     * não precisa distinguir os casos: todos levam ao mesmo 401.
     */
    public Optional<Long> extrairUsuarioId(String token) {
        try {
            Claims conteudo = Jwts.parser()
                    .verifyWith(chave)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return Optional.of(Long.valueOf(conteudo.getSubject()));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public long getValidadeSegundos() {
        return validadeSegundos;
    }
}
