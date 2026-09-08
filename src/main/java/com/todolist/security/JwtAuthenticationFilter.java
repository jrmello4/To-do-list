package com.todolist.security;

import com.todolist.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIXO = "Bearer ";

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest requisicao,
                                    @NonNull HttpServletResponse resposta,
                                    @NonNull FilterChain cadeia)
            throws ServletException, IOException {

        // O filtro apenas popula o contexto quando há um token válido. Negar
        // acesso é responsabilidade das regras de autorização, não daqui:
        // rotas públicas precisam seguir adiante sem token.
        extrairToken(requisicao)
                .flatMap(jwtService::extrairUsuarioId)
                .flatMap(usuarioRepository::findById)
                .map(UsuarioAutenticado::new)
                .filter(UsuarioAutenticado::isEnabled)
                .ifPresent(usuario -> {
                    UsernamePasswordAuthenticationToken autenticacao =
                            new UsernamePasswordAuthenticationToken(
                                    usuario, null, usuario.getAuthorities());
                    autenticacao.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(requisicao));
                    SecurityContextHolder.getContext().setAuthentication(autenticacao);
                });

        cadeia.doFilter(requisicao, resposta);
    }

    private java.util.Optional<String> extrairToken(HttpServletRequest requisicao) {
        String cabecalho = requisicao.getHeader("Authorization");

        if (cabecalho != null && cabecalho.startsWith(PREFIXO)) {
            return java.util.Optional.of(cabecalho.substring(PREFIXO.length()).trim());
        }
        return java.util.Optional.empty();
    }
}
