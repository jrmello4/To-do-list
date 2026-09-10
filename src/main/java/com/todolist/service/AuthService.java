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
import java.time.LocalTime;
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
    private final EventoCalendarioRepository eventoCalendarioRepository;

    @Transactional
    public AuthResponse cadastrar(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("O e-mail informado jÃ¡ estÃ¡ cadastrado.");
        }

        User user = User.builder()
                .nome(request.getNome().trim())
                .email(request.getEmail().trim().toLowerCase())
                .senha(passwordEncoder.encode(request.getSenha()))
                .role(Role.ROLE_USER)
                .build();

        User saved = userRepository.save(user);
        inicializarEsportesConvidado(saved);
        String token = jwtService.gerarToken(saved);

        return AuthResponse.builder()
                .token(token)
                .id(saved.getId())
                .nome(saved.getNome())
                .email(saved.getEmail())
                .role(saved.getRole())
                .build();
    }

    @Transactional
    public AuthResponse autenticar(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().trim().toLowerCase(),
                        request.getSenha()
                )
        );

        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("UsuÃ¡rio", 0L));

        // Contas antigas podem nÃ£o ter preferÃªncias esportivas â€” garante o radar
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

    @Transactional(readOnly = true)
    public User obterUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalArgumentException("Acesso nÃ£o autorizado: usuÃ¡rio nÃ£o autenticado.");
        }

        if (authentication.getPrincipal() instanceof User user) {
            return user;
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("UsuÃ¡rio", 0L));
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
                    .nome("UsuÃ¡rio Convidado")
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
        Tag tagRevisao = tagRepository.findByUsuarioIdAndNomeIgnoreCase(user.getId(), "RevisÃ£o")
                .orElseGet(() -> tagRepository.save(Tag.builder().nome("RevisÃ£o").cor("#10B981").usuario(user).build()));
        Tag tagApi = tagRepository.findByUsuarioIdAndNomeIgnoreCase(user.getId(), "API")
                .orElseGet(() -> tagRepository.save(Tag.builder().nome("API").cor("#8B5CF6").usuario(user).build()));
        Tag tagPessoal = tagRepository.findByUsuarioIdAndNomeIgnoreCase(user.getId(), "Pessoal")
                .orElseGet(() -> tagRepository.save(Tag.builder().nome("Pessoal").cor("#F59E0B").usuario(user).build()));
        Tag tagCasa = tagRepository.findByUsuarioIdAndNomeIgnoreCase(user.getId(), "Casa")
                .orElseGet(() -> tagRepository.save(Tag.builder().nome("Casa").cor("#06B6D4").usuario(user).build()));

        LocalDate hoje = LocalDate.now();

        // 1. Foco / Pomodoro
        Task task1 = Task.builder()
                .titulo("ðŸŽ¯ Explorar Modo Foco Pomodoro")
                .descricao("Inicie o cronÃ´metro Pomodoro de 25 minutos e foque nesta tarefa!")
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
        task1.getSubtarefas().add(Subtask.builder().titulo("Completar sessÃ£o de foco").concluida(false).task(task1).build());
        taskRepository.save(task1);

        // 2. Dashboard / GrÃ¡ficos
        Task task2 = Task.builder()
                .titulo("ðŸ“Š Analisar Dashboard GrÃ¡fico")
                .descricao("Acesse a aba 'Produtividade' para ver a distribuiÃ§Ã£o por categoria e prioridade.")
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
        task2.getSubtarefas().add(Subtask.builder().titulo("Ver grÃ¡fico de categorias").concluida(false).task(task2).build());
        task2.getSubtarefas().add(Subtask.builder().titulo("Ver taxa de pontualidade").concluida(false).task(task2).build());
        taskRepository.save(task2);

        // 3. CrÃ­tica / atrasada
        Task task3 = Task.builder()
                .titulo("âš ï¸ Alerta de Prazo CrÃ­tico")
                .descricao("Tarefa com prazo expirado para testar o alerta no sino ðŸ”” de notificaÃ§Ãµes.")
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

        // 4. ConcluÃ­da
        Task task4 = Task.builder()
                .titulo("ðŸ“„ Gerar RelatÃ³rio Executivo em PDF")
                .descricao("Clique no botÃ£o 'Exportar PDF' no cabeÃ§alho para gerar o relatÃ³rio corporativo.")
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

        // 5. Recorrente
        Task task5 = Task.builder()
                .titulo("ðŸ” Alinhamento Semanal de Sprint")
                .descricao("Tarefa configurada com recorrÃªncia semanal para teste de replicaÃ§Ã£o automÃ¡tica.")
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

        // 6. Projeto pessoal â€” Kanban
        Task task6 = Task.builder()
                .titulo("ðŸš€ LanÃ§ar portfÃ³lio pessoal")
                .descricao("Montar landing page, revisar cases e publicar no domÃ­nio prÃ³prio.")
                .categoria(Categoria.ESTUDOS)
                .prioridade(Prioridade.ALTA)
                .status(StatusTarefa.EM_ANDAMENTO)
                .concluida(false)
                .pomodorosEstimados(8)
                .pomodorosRealizados(3)
                .dataVencimento(hoje.plusDays(10))
                .usuario(user)
                .tags(new HashSet<>(List.of(tagFrontend, tagPessoal)))
                .build();
        task6.getSubtarefas().add(Subtask.builder().titulo("Escrever about me").concluida(true).task(task6).build());
        task6.getSubtarefas().add(Subtask.builder().titulo("Exportar screenshots dos projetos").concluida(false).task(task6).build());
        task6.getSubtarefas().add(Subtask.builder().titulo("Configurar deploy").concluida(false).task(task6).build());
        taskRepository.save(task6);

        // 7. Casa / manutenÃ§Ã£o
        Task task7 = Task.builder()
                .titulo("ðŸ  Trocar filtro de Ã¡gua")
                .descricao("Comprar filtro novo na farmÃ¡cia e agendar troca.")
                .categoria(Categoria.PESSOAL)
                .prioridade(Prioridade.MEDIA)
                .status(StatusTarefa.A_FAZER)
                .concluida(false)
                .dataVencimento(hoje.plusDays(2))
                .usuario(user)
                .tags(new HashSet<>(List.of(tagCasa)))
                .build();
        taskRepository.save(task7);

        // 8. Compras
        Task task8 = Task.builder()
                .titulo("ðŸ›’ Lista do mercado da semana")
                .descricao("Arroz, feijÃ£o, frutas, cafÃ© e itens de limpeza.")
                .categoria(Categoria.PESSOAL)
                .prioridade(Prioridade.BAIXA)
                .status(StatusTarefa.A_FAZER)
                .concluida(false)
                .dataVencimento(hoje.plusDays(1))
                .usuario(user)
                .tags(new HashSet<>(List.of(tagPessoal)))
                .build();
        task8.getSubtarefas().add(Subtask.builder().titulo("Arroz e feijÃ£o").concluida(false).task(task8).build());
        task8.getSubtarefas().add(Subtask.builder().titulo("Frutas da estaÃ§Ã£o").concluida(false).task(task8).build());
        taskRepository.save(task8);

        // 9. SaÃºde
        Task task9 = Task.builder()
                .titulo("ðŸ©º Agendar check-up anual")
                .descricao("Ligar na clÃ­nica e reservar horÃ¡rio com clÃ­nico geral.")
                .categoria(Categoria.SAUDE)
                .prioridade(Prioridade.MEDIA)
                .status(StatusTarefa.A_FAZER)
                .concluida(false)
                .dataVencimento(hoje.plusDays(7))
                .usuario(user)
                .build();
        taskRepository.save(task9);

        // 10. ConcluÃ­da extra (grÃ¡fico mais cheio)
        Task task10 = Task.builder()
                .titulo("âœ… Organizar caixa de entrada do e-mail")
                .descricao("Zerar unread e criar filtros de labels.")
                .categoria(Categoria.TRABALHO)
                .prioridade(Prioridade.BAIXA)
                .status(StatusTarefa.CONCLUIDA)
                .concluida(true)
                .dataVencimento(hoje.minusDays(2))
                .usuario(user)
                .tags(new HashSet<>(List.of(tagRevisao)))
                .build();
        taskRepository.save(task10);
    }

    /** Popula finanÃ§as, hÃ¡bitos, metas, notas e calendÃ¡rio do modo convidado. */
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

        ContaFinanceira invest = contaFinanceiraRepository.save(ContaFinanceira.builder()
                .nome("Investimentos")
                .tipo(TipoConta.INVESTIMENTO)
                .saldoInicial(new BigDecimal("8000.00"))
                .saldoAtual(new BigDecimal("8450.00"))
                .cor("#6366F1")
                .ativo(true)
                .usuario(user)
                .build());

        CategoriaTransacao catMoradia = categoriaTransacaoRepository.save(CategoriaTransacao.builder()
                .nome("Moradia").tipo(TipoTransacao.DESPESA).icone("home").cor("#F59E0B").usuario(user).build());
        CategoriaTransacao catSalario = categoriaTransacaoRepository.save(CategoriaTransacao.builder()
                .nome("SalÃ¡rio").tipo(TipoTransacao.RECEITA).icone("briefcase").cor("#10B981").usuario(user).build());
        CategoriaTransacao catMercado = categoriaTransacaoRepository.save(CategoriaTransacao.builder()
                .nome("Mercado").tipo(TipoTransacao.DESPESA).icone("shopping-cart").cor("#3B82F6").usuario(user).build());
        CategoriaTransacao catLazer = categoriaTransacaoRepository.save(CategoriaTransacao.builder()
                .nome("Lazer").tipo(TipoTransacao.DESPESA).icone("film").cor("#8B5CF6").usuario(user).build());
        CategoriaTransacao catTransporte = categoriaTransacaoRepository.save(CategoriaTransacao.builder()
                .nome("Transporte").tipo(TipoTransacao.DESPESA).icone("car").cor("#06B6D4").usuario(user).build());
        CategoriaTransacao catSaude = categoriaTransacaoRepository.save(CategoriaTransacao.builder()
                .nome("SaÃºde").tipo(TipoTransacao.DESPESA).icone("heartbeat").cor("#EF4444").usuario(user).build());
        CategoriaTransacao catFreela = categoriaTransacaoRepository.save(CategoriaTransacao.builder()
                .nome("Freela").tipo(TipoTransacao.RECEITA).icone("laptop-code").cor("#22C55E").usuario(user).build());

        transacaoRepository.save(Transacao.builder()
                .descricao("SalÃ¡rio Mensal").tipo(TipoTransacao.RECEITA).valor(new BigDecimal("5500.00"))
                .dataVencimento(hoje.withDayOfMonth(5)).dataPagamento(hoje.withDayOfMonth(5))
                .status(StatusTransacao.PAGO).conta(contaPrincipal).categoria(catSalario).usuario(user).build());

        transacaoRepository.save(Transacao.builder()
                .descricao("Aluguel").tipo(TipoTransacao.DESPESA).valor(new BigDecimal("1450.00"))
                .dataVencimento(hoje.plusDays(3)).status(StatusTransacao.PENDENTE)
                .conta(contaPrincipal).categoria(catMoradia).usuario(user).build());

        transacaoRepository.save(Transacao.builder()
                .descricao("Supermercado da semana").tipo(TipoTransacao.DESPESA).valor(new BigDecimal("280.00"))
                .dataVencimento(hoje.minusDays(2)).dataPagamento(hoje.minusDays(2))
                .status(StatusTransacao.PAGO).conta(carteira).categoria(catMercado).usuario(user).build());

        transacaoRepository.save(Transacao.builder()
                .descricao("Cinema").tipo(TipoTransacao.DESPESA).valor(new BigDecimal("65.00"))
                .dataVencimento(hoje.plusDays(1)).status(StatusTransacao.PENDENTE)
                .conta(carteira).categoria(catLazer).usuario(user).build());

        transacaoRepository.save(Transacao.builder()
                .descricao("CombustÃ­vel / apps").tipo(TipoTransacao.DESPESA).valor(new BigDecimal("220.00"))
                .dataVencimento(hoje.minusDays(5)).dataPagamento(hoje.minusDays(5))
                .status(StatusTransacao.PAGO).conta(carteira).categoria(catTransporte).usuario(user).build());

        transacaoRepository.save(Transacao.builder()
                .descricao("Plano de saÃºde").tipo(TipoTransacao.DESPESA).valor(new BigDecimal("390.00"))
                .dataVencimento(hoje.plusDays(8)).status(StatusTransacao.PENDENTE)
                .conta(contaPrincipal).categoria(catSaude).usuario(user).build());

        transacaoRepository.save(Transacao.builder()
                .descricao("Freela landing page").tipo(TipoTransacao.RECEITA).valor(new BigDecimal("1200.00"))
                .dataVencimento(hoje.plusDays(12)).status(StatusTransacao.PENDENTE)
                .conta(contaPrincipal).categoria(catFreela).usuario(user).build());

        transacaoRepository.save(Transacao.builder()
                .descricao("Internet fibra").tipo(TipoTransacao.DESPESA).valor(new BigDecimal("129.90"))
                .dataVencimento(hoje.withDayOfMonth(Math.min(hoje.getDayOfMonth() + 2, 28)))
                .status(StatusTransacao.PENDENTE).conta(contaPrincipal).categoria(catMoradia).usuario(user).build());

        // HÃ¡bitos + streaks
        Habito agua = habitoRepository.save(Habito.builder()
                .nome("Beber 2L de Ãgua").icone("droplet").cor("#06b6d4").ativo(true).usuario(user).build());
        Habito leitura = habitoRepository.save(Habito.builder()
                .nome("Leitura / Estudo 20min").icone("book-open").cor("#8b5cf6").ativo(true).usuario(user).build());
        Habito caminhada = habitoRepository.save(Habito.builder()
                .nome("ExercÃ­cio FÃ­sico / Caminhada").icone("activity").cor("#10b981").ativo(true).usuario(user).build());
        Habito dormir = habitoRepository.save(Habito.builder()
                .nome("Dormir atÃ© 23h").icone("moon").cor("#6366f1").ativo(true).usuario(user).build());

        registroHabitoRepository.save(RegistroHabito.builder().habito(agua).dataRegistro(hoje).concluido(true).build());
        registroHabitoRepository.save(RegistroHabito.builder().habito(agua).dataRegistro(hoje.minusDays(1)).concluido(true).build());
        registroHabitoRepository.save(RegistroHabito.builder().habito(agua).dataRegistro(hoje.minusDays(2)).concluido(true).build());
        registroHabitoRepository.save(RegistroHabito.builder().habito(agua).dataRegistro(hoje.minusDays(3)).concluido(true).build());
        registroHabitoRepository.save(RegistroHabito.builder().habito(leitura).dataRegistro(hoje).concluido(true).build());
        registroHabitoRepository.save(RegistroHabito.builder().habito(leitura).dataRegistro(hoje.minusDays(1)).concluido(true).build());
        registroHabitoRepository.save(RegistroHabito.builder().habito(caminhada).dataRegistro(hoje).concluido(false).build());
        registroHabitoRepository.save(RegistroHabito.builder().habito(caminhada).dataRegistro(hoje.minusDays(1)).concluido(true).build());
        registroHabitoRepository.save(RegistroHabito.builder().habito(dormir).dataRegistro(hoje).concluido(true).build());

        // Metas
        metaRepository.save(Meta.builder()
                .titulo("Reserva de EmergÃªncia").descricao("Guardar 6 meses de custo fixo")
                .categoria("FINANCEIRA").valorAlvo(new BigDecimal("10000.00")).valorAtual(new BigDecimal("3500.00"))
                .unidade("R$").prazo(hoje.plusMonths(6)).cor("#10b981").icone("shield")
                .concluida(false).ativo(true).usuario(user).build());
        metaRepository.save(Meta.builder()
                .titulo("Ler 12 Livros no Ano").descricao("Manter o hÃ¡bito de leitura")
                .categoria("ESTUDO").valorAlvo(new BigDecimal("12.00")).valorAtual(new BigDecimal("4.00"))
                .unidade("livros").prazo(hoje.plusMonths(4)).cor("#6366f1").icone("book")
                .concluida(false).ativo(true).usuario(user).build());
        metaRepository.save(Meta.builder()
                .titulo("Viagem para o Chile").descricao("Passagens + hospedagem para 7 dias")
                .categoria("PESSOAL").valorAlvo(new BigDecimal("8000.00")).valorAtual(new BigDecimal("2100.00"))
                .unidade("R$").prazo(hoje.plusMonths(9)).cor("#06b6d4").icone("plane")
                .concluida(false).ativo(true).usuario(user).build());
        metaRepository.save(Meta.builder()
                .titulo("Correr 10km").descricao("Preparar prova de 10k no parque")
                .categoria("SAUDE").valorAlvo(new BigDecimal("10.00")).valorAtual(new BigDecimal("6.50"))
                .unidade("km").prazo(hoje.plusMonths(2)).cor("#f59e0b").icone("running")
                .concluida(false).ativo(true).usuario(user).build());

        // Notas
        notaRapidaRepository.save(NotaRapida.builder()
                .titulo("Ideias da semana")
                .conteudo("Este Ã© o seu bloco de notas rÃ¡pidas! Use para rascunhos, links Ãºteis ou insights.")
                .cor("#fff7ed").fixada(true).usuario(user).build());
        notaRapidaRepository.save(NotaRapida.builder()
                .titulo("Compras online")
                .conteudo("Comparar preÃ§o de monitor 27\\\" e cadeira ergonÃ´mica antes da Black Friday.")
                .cor("#eff6ff").fixada(false).usuario(user).build());

        // CalendÃ¡rio
        eventoCalendarioRepository.save(EventoCalendario.builder()
                .titulo("Dentista").descricao("Limpeza semestral")
                .dataEvento(hoje.plusDays(4)).horaInicio(LocalTime.of(10, 0)).horaFim(LocalTime.of(11, 0))
                .cor("#06b6d4").categoria("SaÃºde").ativo(true).usuario(user).build());
        eventoCalendarioRepository.save(EventoCalendario.builder()
                .titulo("AlmoÃ§o com a equipe").descricao("Comemorar release do sprint")
                .dataEvento(hoje.plusDays(2)).horaInicio(LocalTime.of(12, 30)).horaFim(LocalTime.of(14, 0))
                .cor("#8b5cf6").categoria("Trabalho").ativo(true).usuario(user).build());
        eventoCalendarioRepository.save(EventoCalendario.builder()
                .titulo("AniversÃ¡rio da Ana").descricao("Jantar Ã s 20h")
                .dataEvento(hoje.plusDays(6)).horaInicio(LocalTime.of(20, 0))
                .cor("#f59e0b").categoria("Pessoal").ativo(true).usuario(user).build());
        eventoCalendarioRepository.save(EventoCalendario.builder()
                .titulo("ReuniÃ£o 1:1 com gestor").descricao("Feedback trimestral")
                .dataEvento(hoje.plusDays(1)).horaInicio(LocalTime.of(9, 0)).horaFim(LocalTime.of(9, 45))
                .cor("#3b82f6").categoria("Trabalho").ativo(true).usuario(user).build());
    }

    private void inicializarEsportesConvidado(User user) {
        if (preferenciaEsporteRepository.findByUsuarioAndAtivoTrue(user).size() > 0) {
            return;
        }
        preferenciaEsporteRepository.saveAll(List.of(
                PreferenciaEsporte.builder().esporte("FUTEBOL").nomeInteresse("Premier League")
                        .icone("PL").cor("#3d195b").ativo(true).usuario(user).build(),
                PreferenciaEsporte.builder().esporte("FUTEBOL").nomeInteresse("Champions League")
                        .icone("UCL").cor("#0b1c3d").ativo(true).usuario(user).build(),
                PreferenciaEsporte.builder().esporte("FUTEBOL").nomeInteresse("Brasileirao")
                        .icone("BR").cor("#009c3b").ativo(true).usuario(user).build(),
                PreferenciaEsporte.builder().esporte("FUTEBOL").nomeInteresse("Libertadores")
                        .icone("LIB").cor("#c4a35a").ativo(true).usuario(user).build(),
                PreferenciaEsporte.builder().esporte("UFC").nomeInteresse("UFC")
                        .icone("UFC").cor("#ef4444").ativo(true).usuario(user).build(),
                PreferenciaEsporte.builder().esporte("F1").nomeInteresse("Formula 1")
                        .icone("F1").cor("#e10600").ativo(true).usuario(user).build(),
                PreferenciaEsporte.builder().esporte("BASQUETE").nomeInteresse("NBA")
                        .icone("NBA").cor("#f59e0b").ativo(true).usuario(user).build(),
                PreferenciaEsporte.builder().esporte("NFL").nomeInteresse("NFL")
                        .icone("NFL").cor("#013369").ativo(true).usuario(user).build()
        ));
    }
}