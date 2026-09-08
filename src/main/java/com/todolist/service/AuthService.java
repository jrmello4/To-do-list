package com.todolist.service;

import com.todolist.dto.AuthResponse;
import com.todolist.dto.LoginRequest;
import com.todolist.dto.RegistroRequest;
import com.todolist.dto.UsuarioResponse;
import com.todolist.entity.Usuario;
import com.todolist.exception.EmailJaCadastradoException;
import com.todolist.exception.ResourceNotFoundException;
import com.todolist.repository.UsuarioRepository;
import com.todolist.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                .build();
    }
}
