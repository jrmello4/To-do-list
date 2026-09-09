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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

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
