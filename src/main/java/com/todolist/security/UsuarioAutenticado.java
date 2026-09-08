package com.todolist.security;

import com.todolist.entity.Usuario;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Principal em uso durante a requisição. Carrega o id porque é ele que as
 * consultas usam para filtrar por dono; carrega o hash apenas para o fluxo de
 * login do Spring Security, e nunca é serializado para o cliente.
 */
@Getter
public class UsuarioAutenticado implements UserDetails {

    private final Long id;
    private final String nome;
    private final String email;
    private final String senhaHash;
    private final boolean ativo;

    public UsuarioAutenticado(Usuario usuario) {
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.email = usuario.getEmail();
        this.senhaHash = usuario.getSenhaHash();
        this.ativo = Boolean.TRUE.equals(usuario.getAtivo());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Sem papéis por enquanto: toda conta tem o mesmo acesso, aos próprios dados.
        return List.of();
    }

    @Override
    public String getPassword() {
        return senhaHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return ativo;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}
