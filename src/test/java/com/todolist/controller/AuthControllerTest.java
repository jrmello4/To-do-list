package com.todolist.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.todolist.dto.AuthResponse;
import com.todolist.dto.LoginRequest;
import com.todolist.dto.RegisterRequest;
import com.todolist.dto.UserResponse;
import com.todolist.entity.Role;
import com.todolist.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import com.todolist.config.SecurityConfig;
import com.todolist.security.JwtAuthenticationFilter;

@WebMvcTest(
        controllers = AuthController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {SecurityConfig.class, JwtAuthenticationFilter.class}
        ),
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Test
    @DisplayName("POST /api/auth/cadastro - Deve cadastrar usuário e retornar token")
    void deveCadastrarUsuario() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .nome("Adenilson")
                .email("dev@teste.com")
                .senha("123456")
                .build();

        AuthResponse response = AuthResponse.builder()
                .token("jwt-token-fake")
                .id(1L)
                .nome("Adenilson")
                .email("dev@teste.com")
                .role(Role.ROLE_USER)
                .build();

        when(authService.cadastrar(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token-fake"))
                .andExpect(jsonPath("$.nome").value("Adenilson"));
    }

    @Test
    @DisplayName("POST /api/auth/login - Deve autenticar e retornar token")
    void deveFazerLogin() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("dev@teste.com")
                .senha("123456")
                .build();

        AuthResponse response = AuthResponse.builder()
                .token("jwt-token-fake")
                .id(1L)
                .nome("Adenilson")
                .email("dev@teste.com")
                .role(Role.ROLE_USER)
                .build();

        when(authService.autenticar(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-fake"));
    }

    @Test
    @DisplayName("GET /api/auth/me - Deve retornar perfil do usuário")
    void deveRetornarPerfil() throws Exception {
        UserResponse response = UserResponse.builder()
                .id(1L)
                .nome("Adenilson")
                .email("dev@teste.com")
                .role(Role.ROLE_USER)
                .build();

        when(authService.obterPerfilAtual()).thenReturn(response);

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Adenilson"))
                .andExpect(jsonPath("$.email").value("dev@teste.com"));
    }

    @Test
    @DisplayName("POST /api/auth/convidado - Deve autenticar como convidado e retornar token")
    void deveAutenticarComoConvidado() throws Exception {
        AuthResponse response = AuthResponse.builder()
                .token("jwt-guest-token")
                .id(99L)
                .nome("Usuário Convidado")
                .email("convidado@todolist.local")
                .role(Role.ROLE_USER)
                .build();

        when(authService.autenticarComoConvidado()).thenReturn(response);

        mockMvc.perform(post("/api/auth/convidado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-guest-token"))
                .andExpect(jsonPath("$.nome").value("Usuário Convidado"))
                .andExpect(jsonPath("$.email").value("convidado@todolist.local"));
    }

    @Test
    @DisplayName("POST /api/auth/login - Deve retornar 401 para credenciais incorretas")
    void deveRetornar401ParaCredenciaisInvalidas() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("dev@teste.com")
                .senha("senhaerrada")
                .build();

        when(authService.autenticar(any(LoginRequest.class)))
                .thenThrow(new org.springframework.security.authentication.BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.mensagem").value("Credenciais inválidas: e-mail ou senha incorretos."));
    }
}
