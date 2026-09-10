package com.todolist.service;

import com.todolist.dto.AuthResponse;
import com.todolist.dto.LoginRequest;
import com.todolist.dto.RegisterRequest;
import com.todolist.dto.UserResponse;
import com.todolist.entity.*;
import com.todolist.exception.ResourceNotFoundException;
import com.todolist.repository.*;
import com.todolist.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final ContaFinanceiraRepository contaFinanceiraRepository;
    private final CategoriaTransacaoRepository categoriaTransacaoRepository;
    private final TransacaoRepository transacaoRepository;
    private final HabitoRepository habitoRepository;
    private final RegistroHabitoRepository registroHabitoRepository;
    private final MetaRepository metaRepository;
    private final NotaRapidaRepository notaRapidaRepository;
    private final PreferenciaEsporteRepository preferenciaEsporteRepository;

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

        if (taskRepository.countByUsuarioIdAndDeletadaFalse(user.getId()) == 0) {
            inicializarDadosConvidado(user);
        }
        if (contaFinanceiraRepository.findByUsuarioIdAndAtivoTrueOrderByNomeAsc(user.getId()).isEmpty()) {
            inicializarLifeHubConvidado(user);
        }
        if (preferenciaEsporteRepository.findByUsuarioAndAtivoTrue(user).isEmpty()) {
            inicializarEsportesConvidado(user);
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

    /** Popula finanças, hábitos, metas e notas do modo convidado (sem seed em GET). */
    private void inicializarLifeHubConvidado(User user) {
        LocalDate hoje = LocalDate.now();

        ContaFinanceira contaPrincipal = contaFinanceiraRepository.save(ContaFinanceira.builder()
                .nome("Nubank")
                .tipo(TipoConta.CORRENTE)
                .saldoInicial(new BigDecimal("2500.00"))
                .saldoAtual(new BigDecimal("3200.00"))
                .cor("#820AD1")
                .ativo(true)
                .usuario(user)
                .build());

        ContaFinanceira carteira = contaFinanceiraRepository.save(ContaFinanceira.builder()
                .nome("Carteira")
                .tipo(TipoConta.DINHEIRO)
                .saldoInicial(new BigDecimal("180.00"))
                .saldoAtual(new BigDecimal("120.00"))
                .cor("#10B981")
                .ativo(true)
                .usuario(user)
                .build());

        CategoriaTransacao catMoradia = categoriaTransacaoRepository.save(CategoriaTransacao.builder()
                .nome("Moradia").tipo(TipoTransacao.DESPESA).icone("home").cor("#F59E0B").usuario(user).build());
        CategoriaTransacao catSalario = categoriaTransacaoRepository.save(CategoriaTransacao.builder()
                .nome("Salário").tipo(TipoTransacao.RECEITA).icone("briefcase").cor("#10B981").usuario(user).build());
        CategoriaTransacao catMercado = categoriaTransacaoRepository.save(CategoriaTransacao.builder()
                .nome("Mercado").tipo(TipoTransacao.DESPESA).icone("shopping-cart").cor("#3B82F6").usuario(user).build());
        CategoriaTransacao catLazer = categoriaTransacaoRepository.save(CategoriaTransacao.builder()
                .nome("Lazer").tipo(TipoTransacao.DESPESA).icone("film").cor("#8B5CF6").usuario(user).build());

        transacaoRepository.save(Transacao.builder()
                .descricao("Salário Mensal")
                .tipo(TipoTransacao.RECEITA)
                .valor(new BigDecimal("5500.00"))
                .dataVencimento(hoje.withDayOfMonth(5))
                .dataPagamento(hoje.withDayOfMonth(5))
                .status(StatusTransacao.PAGO)
                .conta(contaPrincipal)
                .categoria(catSalario)
                .usuario(user)
                .build());

        transacaoRepository.save(Transacao.builder()
                .descricao("Aluguel")
                .tipo(TipoTransacao.DESPESA)
                .valor(new BigDecimal("1450.00"))
                .dataVencimento(hoje.plusDays(3))
                .status(StatusTransacao.PENDENTE)
                .conta(contaPrincipal)
                .categoria(catMoradia)
                .usuario(user)
                .build());

        transacaoRepository.save(Transacao.builder()
                .descricao("Supermercado da semana")
                .tipo(TipoTransacao.DESPESA)
                .valor(new BigDecimal("280.00"))
                .dataVencimento(hoje.minusDays(2))
                .dataPagamento(hoje.minusDays(2))
                .status(StatusTransacao.PAGO)
                .conta(carteira)
                .categoria(catMercado)
                .usuario(user)
                .build());

        transacaoRepository.save(Transacao.builder()
                .descricao("Cinema")
                .tipo(TipoTransacao.DESPESA)
                .valor(new BigDecimal("65.00"))
                .dataVencimento(hoje.plusDays(1))
                .status(StatusTransacao.PENDENTE)
                .conta(carteira)
                .categoria(catLazer)
                .usuario(user)
                .build());

        Habito agua = habitoRepository.save(Habito.builder()
                .nome("Beber 2L de Água").icone("droplet").cor("#06b6d4").ativo(true).usuario(user).build());
        Habito leitura = habitoRepository.save(Habito.builder()
                .nome("Leitura / Estudo 20min").icone("book-open").cor("#8b5cf6").ativo(true).usuario(user).build());
        Habito caminhada = habitoRepository.save(Habito.builder()
                .nome("Exercício Físico / Caminhada").icone("activity").cor("#10b981").ativo(true).usuario(user).build());

        registroHabitoRepository.save(RegistroHabito.builder().habito(agua).dataRegistro(hoje).concluido(true).build());
        registroHabitoRepository.save(RegistroHabito.builder().habito(agua).dataRegistro(hoje.minusDays(1)).concluido(true).build());
        registroHabitoRepository.save(RegistroHabito.builder().habito(agua).dataRegistro(hoje.minusDays(2)).concluido(true).build());
        registroHabitoRepository.save(RegistroHabito.builder().habito(leitura).dataRegistro(hoje).concluido(true).build());
        registroHabitoRepository.save(RegistroHabito.builder().habito(leitura).dataRegistro(hoje.minusDays(1)).concluido(false).build());

        metaRepository.save(Meta.builder()
                .titulo("Reserva de Emergência")
                .descricao("Guardar 6 meses de custo fixo")
                .categoria("FINANCEIRA")
                .valorAlvo(new BigDecimal("10000.00"))
                .valorAtual(new BigDecimal("3500.00"))
                .unidade("R$")
                .prazo(hoje.plusMonths(6))
                .cor("#10b981")
                .icone("shield")
                .concluida(false)
                .ativo(true)
                .usuario(user)
                .build());

        metaRepository.save(Meta.builder()
                .titulo("Ler 12 Livros no Ano")
                .descricao("Manter o hábito de leitura")
                .categoria("ESTUDO")
                .valorAlvo(new BigDecimal("12.00"))
                .valorAtual(new BigDecimal("4.00"))
                .unidade("livros")
                .prazo(hoje.plusMonths(4))
                .cor("#6366f1")
                .icone("book")
                .concluida(false)
                .ativo(true)
                .usuario(user)
                .build());

        notaRapidaRepository.save(NotaRapida.builder()
                .titulo("Ideias da semana")
                .conteudo("Este é o seu bloco de notas rápidas! Use para rascunhos, links úteis ou insights.")
                .cor("#fff7ed")
                .fixada(true)
                .usuario(user)
                .build());
    }

    private void inicializarEsportesConvidado(User user) {
        preferenciaEsporteRepository.saveAll(List.of(
                PreferenciaEsporte.builder().esporte("FUTEBOL").nomeInteresse("Flamengo").icone("⚽").cor("#10b981").ativo(true).usuario(user).build(),
                PreferenciaEsporte.builder().esporte("UFC").nomeInteresse("UFC / MMA").icone("🥊").cor("#ef4444").ativo(true).usuario(user).build(),
                PreferenciaEsporte.builder().esporte("BASQUETE").nomeInteresse("NBA").icone("🏀").cor("#f59e0b").ativo(true).usuario(user).build()
        ));
    }
}
