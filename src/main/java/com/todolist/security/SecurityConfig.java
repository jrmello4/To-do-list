package com.todolist.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.DelegatingRequestMatcherHeaderWriter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String CABECALHO_CSP = "Content-Security-Policy";

    /**
     * Política da interface, sem nenhuma exceção.
     *
     * Sai barata porque a página não carrega nada de fora e não tem script nem
     * estilo embutido — os quatro atributos `style` que existiam no HTML
     * viraram classe justamente para esta política não precisar abrir mão de
     * nada. Sem 'unsafe-inline' em lugar nenhum, um trecho de texto injetado
     * não vira código; e com connect-src 'self', o que fosse lido não teria
     * para onde ser mandado.
     */
    private static final String CSP_DA_APLICACAO =
            "default-src 'self'; "
                    + "script-src 'self'; "
                    + "style-src 'self'; "
                    + "img-src 'self' data:; "
                    + "connect-src 'self'; "
                    + "form-action 'self'; "
                    + "base-uri 'self'; "
                    + "frame-ancestors 'none'; "
                    + "object-src 'none'";

    /**
     * Política do Swagger UI, que precisa de estilo embutido.
     *
     * O Swagger UI é React e aplica estilo por atributo nos próprios
     * componentes; com style-src 'self' o navegador recusa e a página fica
     * quebrada — foi o que apareceu ao carregá-la num navegador de verdade.
     *
     * A alternativa seria afrouxar a política da interface inteira por causa
     * de uma página de documentação, e é justamente essa troca que não vale:
     * a interface é onde os dados de quem usa aparecem. Aqui a concessão é só
     * de estilo, e continua valendo o que importa — script-src sem
     * 'unsafe-inline' e nada vindo de fora.
     */
    private static final String CSP_DA_DOCUMENTACAO =
            "default-src 'self'; "
                    + "script-src 'self'; "
                    + "style-src 'self' 'unsafe-inline'; "
                    + "img-src 'self' data:; "
                    + "connect-src 'self'; "
                    + "form-action 'self'; "
                    + "base-uri 'self'; "
                    + "frame-ancestors 'none'; "
                    + "object-src 'none'";

    /**
     * Rotas do Swagger e do JSON do OpenAPI.
     *
     * Escrito à mão em vez de AntPathRequestMatcher porque a API de casamento
     * de rotas do Spring Security mudou de nome entre versões, e um prefixo de
     * caminho não precisa de nada disso. O caminho do contexto sai fora para a
     * comparação valer também quando a aplicação não está servida na raiz.
     */
    private static final RequestMatcher DOCUMENTACAO = requisicao -> {
        String caminho = requisicao.getRequestURI()
                .substring(requisicao.getContextPath().length());

        return caminho.startsWith("/swagger-ui") || caminho.startsWith("/api-docs");
    };

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RespostaDeErroDeSeguranca respostaDeErro;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // API sem sessão e sem formulário: não há cookie de sessão para
                // um site terceiro reaproveitar, então CSRF não se aplica.
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sessao ->
                        sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(rotas -> rotas
                        // Cadastro e login precisam ser públicos, senão não há
                        // como obter o primeiro token.
                        .requestMatchers("/api/auth/registrar", "/api/auth/login").permitAll()
                        // A interface web também: sem ela não se chega à tela de entrada.
                        // sw.js e manifest ficam na raiz: sem estarem aqui, o
                        // service worker nem chega a registrar.
                        .requestMatchers(HttpMethod.GET,
                                "/", "/index.html", "/favicon.svg", "/css/**", "/js/**",
                                "/sw.js", "/manifest.webmanifest")
                        .permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/api-docs/**")
                        .permitAll()
                        .requestMatchers("/error").permitAll()
                        // O health check de quem hospeda roda sem credencial:
                        // atrás de autenticação, ele leria a aplicação como
                        // fora do ar e derrubaria o deploy.
                        .requestMatchers("/actuator/health").permitAll()
                        .anyRequest().authenticated())
                // O Spring já manda X-Frame-Options e nosniff por padrão, mas não
                // uma política de conteúdo. Duas, aqui — ver as constantes
                // abaixo para o porquê de não ser uma só.
                .headers(cabecalhos -> cabecalhos
                        .addHeaderWriter(new DelegatingRequestMatcherHeaderWriter(
                                new NegatedRequestMatcher(DOCUMENTACAO),
                                new StaticHeadersWriter(CABECALHO_CSP, CSP_DA_APLICACAO)))
                        .addHeaderWriter(new DelegatingRequestMatcherHeaderWriter(
                                DOCUMENTACAO,
                                new StaticHeadersWriter(CABECALHO_CSP, CSP_DA_DOCUMENTACAO)))
                        .referrerPolicy(referencia ->
                                referencia.policy(ReferrerPolicy.SAME_ORIGIN)))
                .exceptionHandling(erros -> erros
                        .authenticationEntryPoint(respostaDeErro)
                        .accessDeniedHandler(respostaDeErro))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuracao)
            throws Exception {
        return configuracao.getAuthenticationManager();
    }
}
