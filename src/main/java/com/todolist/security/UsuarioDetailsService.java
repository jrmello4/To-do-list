package com.todolist.security;

import com.todolist.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email) {
        return usuarioRepository.findByEmail(email)
                .map(UsuarioAutenticado::new)
                // Mensagem genérica de propósito: dizer "e-mail não encontrado"
                // permitiria descobrir quais e-mails têm conta.
                .orElseThrow(() -> new UsernameNotFoundException("Credenciais inválidas"));
    }
}
