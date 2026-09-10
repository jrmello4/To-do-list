package com.todolist.service;

import com.todolist.dto.AuthResponse;
import com.todolist.dto.LoginRequest;
import com.todolist.dto.RegistroRequest;
import com.todolist.dto.TrocaDeSenhaRequest;
import com.todolist.dto.PreferenciasRequest;
import com.todolist.dto.UsuarioResponse;
import com.todolist.entity.Usuario;
import com.todolist.exception.EmailJaCadastradoException;
import com.todolist.exception.ResourceNotFoundException;
import com.todolist.exception.SenhaAtualIncorretaException;
import com.todolist.repository.UsuarioRepository;
import com.todolist.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse registrar(RegistroRequest request) {
        String email = normalizar(request.getEmail());

        if (usuarioRepository.existsByEmail(email)) {
            throw new EmailJaCadastradoException(email);
        }

        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .nome(request.getNome().trim())
                .email(email)
                .senhaHash(passwordEncoder.encode(request.getSenha()))
                .build());

        return comToken(usuario);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = normalizar(request.getEmail());

        // Lança BadCredentialsException quando a senha não confere; o
        // GlobalExceptionHandler traduz para 401 sem revelar qual dos dois
        // campos estava errado.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getSenha()));

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", null));

        return comToken(usuario);
    }

    @Transactional
    public UsuarioResponse salvarPreferencias(Long usuarioId, PreferenciasRequest request) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", usuarioId));

        if (request.getLembretesAtivos() != null) {
            usuario.setLembretesAtivos(request.getLembretesAtivos());
        }
        if (request.getHoraLembrete() != null) {
            usuario.setHoraLembrete(request.getHoraLembrete());
        }
        if (request.getFusoHorario() != null && !request.getFusoHorario().isBlank()) {
            // Validado aqui: um fuso inválido gravado agora só apareceria mais
            // tarde, na varredura, como uma conta que nunca recebe lembrete.
            try {
                ZoneId.of(request.getFusoHorario().trim());
            } catch (Exception e) {
                throw new IllegalArgumentException(
                        "Fuso horário desconhecido: " + request.getFusoHorario());
            }
            usuario.setFusoHorario(request.getFusoHorario().trim());
        }

        return toResponse(usuario);
    }

    /**
     * Troca a senha e derruba os tokens antigos.
     *
     * A senha atual é exigida mesmo já havendo token válido em mãos: sem isso,
     * um token roubado bastaria para trocar a senha e tomar a conta de vez.
     *
     * O incremento da versão é o que faz a troca valer alguma coisa. Sem ele,
     * quem tivesse pegado um token continuaria entrando com ele por até 24h
     * depois da senha trocada — que é exatamente quando a pessoa troca.
     */
    @Transactional
    public AuthResponse trocarSenha(Long usuarioId, TrocaDeSenhaRequest request) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", usuarioId));

        if (!passwordEncoder.matches(request.getSenhaAtual(), usuario.getSenhaHash())) {
            throw new SenhaAtualIncorretaException();
        }

        usuario.setSenhaHash(passwordEncoder.encode(request.getNovaSenha()));
        usuario.setTokenVersion(JwtService.versaoDe(usuario) + 1);

        // Devolve um token já na versão nova: quem trocou a senha na própria
        // aba não deveria ser expulso pela troca que acabou de fazer.
        return comToken(usuario);
    }

    /**
     * Invalida todos os tokens da conta, inclusive o que fez esta chamada.
     *
     * É o que existe no lugar de um "sair" de servidor: com token sem sessão,
     * apagar o token do navegador não impede quem já tenha uma cópia dele.
     */
    @Transactional
    public void sairDeTodosOsAparelhos(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", usuarioId));

        usuario.setTokenVersion(JwtService.versaoDe(usuario) + 1);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse perfil(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .map(AuthService::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", usuarioId));
    }

    /**
     * E-mail em minúsculas: sem isso "Ana@x.com" e "ana@x.com" criariam duas
     * contas, porque a restrição de unicidade é sobre o valor gravado.
     */
    private String normalizar(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private AuthResponse comToken(Usuario usuario) {
        return AuthResponse.builder()
                .token(jwtService.gerar(usuario))
                .expiraEm(jwtService.getValidadeSegundos())
                .usuario(toResponse(usuario))
                .build();
    }

    private static UsuarioResponse toResponse(Usuario usuario) {
        return UsuarioResponse.builder()
                .id(usuario.getId())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .dataCriacao(usuario.getDataCriacao())
                .lembretesAtivos(usuario.getLembretesAtivos())
                .horaLembrete(usuario.getHoraLembrete())
                .fusoHorario(usuario.getFusoHorario())
                .build();
    }
}
