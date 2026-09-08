package com.todolist.service;

import com.todolist.dto.AuthResponse;
import com.todolist.dto.LoginRequest;
import com.todolist.dto.RegisterRequest;
import com.todolist.dto.UserResponse;
import com.todolist.entity.*;
import com.todolist.exception.ResourceNotFoundException;
import com.todolist.repository.TagRepository;
import com.todolist.repository.TaskRepository;
import com.todolist.repository.UserRepository;
import com.todolist.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TaskRepository taskRepository;
    private final TagRepository tagRepository;

    @Transactional
    public AuthResponse cadastrar(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("O e-mail informado já está cadastrado.");
        }

        User user = User.builder()
                .nome(request.getNome().trim())
                .email(request.getEmail().trim().toLowerCase())
                .senha(passwordEncoder.encode(request.getSenha()))
                .role(Role.ROLE_USER)
                .build();

        User saved = userRepository.save(user);
        String token = jwtService.gerarToken(saved);

        return AuthResponse.builder()
                .token(token)
                .id(saved.getId())
                .nome(saved.getNome())
                .email(saved.getEmail())
                .role(saved.getRole())
                .build();
    }

    public AuthResponse autenticar(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().trim().toLowerCase(),
                        request.getSenha()
                )
        );

        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", 0L));

        String token = jwtService.gerarToken(user);

        return AuthResponse.builder()
                .token(token)
                .id(user.getId())
                .nome(user.getNome())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Transactional(readOnly = true)
    public User obterUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalArgumentException("Acesso não autorizado: usuário não autenticado.");
        }

        if (authentication.getPrincipal() instanceof User user) {
            return user;
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", 0L));
    }

    @Transactional(readOnly = true)
    public UserResponse obterPerfilAtual() {
        User user = obterUsuarioAutenticado();
        return UserResponse.builder()
                .id(user.getId())
                .nome(user.getNome())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Transactional
    public AuthResponse autenticarComoConvidado() {
        String guestEmail = "convidado@todolist.local";
        User user = userRepository.findByEmail(guestEmail).orElseGet(() -> {
            User guest = User.builder()
                    .nome("Usuário Convidado")
                    .email(guestEmail)
                    .senha(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .role(Role.ROLE_USER)
                    .build();
            return userRepository.save(guest);
        });

        // Inicializa dados de demonstração caso o usuário convidado não tenha tarefas
        if (taskRepository.countByUsuarioIdAndDeletadaFalse(user.getId()) == 0) {
            inicializarDadosConvidado(user);
        }

        String token = jwtService.gerarToken(user);

        return AuthResponse.builder()
                .token(token)
                .id(user.getId())
                .nome(user.getNome())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    private void inicializarDadosConvidado(User user) {
        Tag tagFrontend = tagRepository.findByUsuarioIdAndNomeIgnoreCase(user.getId(), "Frontend")
                .orElseGet(() -> tagRepository.save(Tag.builder().nome("Frontend").cor("#3B82F6").usuario(user).build()));
        Tag tagUrgente = tagRepository.findByUsuarioIdAndNomeIgnoreCase(user.getId(), "Urgente")
                .orElseGet(() -> tagRepository.save(Tag.builder().nome("Urgente").cor("#EF4444").usuario(user).build()));
        Tag tagRevisao = tagRepository.findByUsuarioIdAndNomeIgnoreCase(user.getId(), "Revisão")
                .orElseGet(() -> tagRepository.save(Tag.builder().nome("Revisão").cor("#10B981").usuario(user).build()));
        Tag tagApi = tagRepository.findByUsuarioIdAndNomeIgnoreCase(user.getId(), "API")
                .orElseGet(() -> tagRepository.save(Tag.builder().nome("API").cor("#8B5CF6").usuario(user).build()));

        LocalDate hoje = LocalDate.now();

        // 1. Tarefa Pomodoro / Foco (Em Andamento)
        Task task1 = Task.builder()
                .titulo("🎯 Explorar Modo Foco Pomodoro")
                .descricao("Inicie o cronômetro Pomodoro de 25 minutos e foque nesta tarefa!")
                .categoria(Categoria.TRABALHO)
                .prioridade(Prioridade.ALTA)
                .status(StatusTarefa.EM_ANDAMENTO)
                .concluida(false)
                .pomodorosEstimados(4)
                .pomodorosRealizados(2)
                .dataVencimento(hoje.plusDays(1))
                .usuario(user)
                .tags(new HashSet<>(List.of(tagFrontend)))
                .build();
        task1.getSubtarefas().add(Subtask.builder().titulo("Iniciar timer de 25 min").concluida(true).task(task1).build());
        task1.getSubtarefas().add(Subtask.builder().titulo("Completar sessão de foco").concluida(false).task(task1).build());
        taskRepository.save(task1);

        // 2. Tarefa Dashboard / Gráficos (A Fazer)
        Task task2 = Task.builder()
                .titulo("📊 Analisar Dashboard Gráfico")
                .descricao("Acesse a aba 'Gráficos & Métricas' para ver a distribuição por categoria e prioridade.")
                .categoria(Categoria.ESTUDOS)
                .prioridade(Prioridade.MEDIA)
                .status(StatusTarefa.A_FAZER)
                .concluida(false)
                .pomodorosEstimados(2)
                .pomodorosRealizados(0)
                .dataVencimento(hoje)
                .usuario(user)
                .tags(new HashSet<>(List.of(tagApi)))
                .build();
        task2.getSubtarefas().add(Subtask.builder().titulo("Ver gráfico de categorias").concluida(false).task(task2).build());
        task2.getSubtarefas().add(Subtask.builder().titulo("Ver taxa de pontualidade").concluida(false).task(task2).build());
        taskRepository.save(task2);

        // 3. Tarefa Crítica para Notificações (Vencimento ontem para acionar alerta atrasada)
        Task task3 = Task.builder()
                .titulo("⚠️ Alerta de Prazo Crítico")
                .descricao("Tarefa com prazo expirado para testar o alerta no sino 🔔 de notificações.")
                .categoria(Categoria.GERAL)
                .prioridade(Prioridade.URGENTE)
                .status(StatusTarefa.A_FAZER)
                .concluida(false)
                .pomodorosEstimados(1)
                .pomodorosRealizados(0)
                .dataVencimento(hoje.minusDays(1))
                .usuario(user)
                .tags(new HashSet<>(List.of(tagUrgente)))
                .build();
        taskRepository.save(task3);

        // 4. Tarefa Concluída / PDF
        Task task4 = Task.builder()
                .titulo("📄 Gerar Relatório Executivo em PDF")
                .descricao("Clique no botão 'Exportar PDF' no cabeçalho para gerar o relatório corporativo.")
                .categoria(Categoria.FINANCAS)
                .prioridade(Prioridade.BAIXA)
                .status(StatusTarefa.CONCLUIDA)
                .concluida(true)
                .pomodorosEstimados(1)
                .pomodorosRealizados(1)
                .usuario(user)
                .tags(new HashSet<>(List.of(tagRevisao)))
                .build();
        taskRepository.save(task4);

        // 5. Tarefa Recorrente Semanal
        Task task5 = Task.builder()
                .titulo("🔁 Alinhamento Semanal de Sprint")
                .descricao("Tarefa configurada com recorrência semanal para teste de replicação automática.")
                .categoria(Categoria.TRABALHO)
                .prioridade(Prioridade.ALTA)
                .status(StatusTarefa.A_FAZER)
                .concluida(false)
                .recorrencia(Recorrencia.SEMANAL)
                .pomodorosEstimados(2)
                .pomodorosRealizados(0)
                .dataVencimento(hoje.plusDays(3))
                .usuario(user)
                .build();
        taskRepository.save(task5);
    }
}
