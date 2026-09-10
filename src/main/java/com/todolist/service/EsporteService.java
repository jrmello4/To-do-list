package com.todolist.service;

import com.todolist.config.EsportesApiProperties;
import com.todolist.dto.EsporteEventoResponse;
import com.todolist.dto.EsporteJogoResponse;
import com.todolist.dto.PreferenciaEsporteRequest;
import com.todolist.entity.EventoCalendario;
import com.todolist.entity.PreferenciaEsporte;
import com.todolist.entity.User;
import com.todolist.exception.ResourceNotFoundException;
import com.todolist.repository.EventoCalendarioRepository;
import com.todolist.repository.PreferenciaEsporteRepository;
import com.todolist.service.esportes.EsporteEventMapper;
import com.todolist.service.esportes.TheSportsDbClient;
import com.todolist.service.esportes.TheSportsDbDtos;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EsporteService {

    private static final Logger log = LoggerFactory.getLogger(EsporteService.class);

    private final PreferenciaEsporteRepository preferenciaRepository;
    private final EventoCalendarioRepository eventoCalendarioRepository;
    private final AuthService authService;
    private final TheSportsDbClient sportsDbClient;
    private final EsporteEventMapper eventMapper;
    private final EsportesApiProperties props;

    // --- Preferências de Esportes do Usuário ---
    @Transactional(readOnly = true)
    public List<PreferenciaEsporte> obterPreferenciasDoUsuario() {
        User usuario = authService.obterUsuarioAutenticado();
        return preferenciaRepository.findByUsuarioAndAtivoTrue(usuario);
    }

    @Transactional
    public PreferenciaEsporte adicionarPreferencia(PreferenciaEsporteRequest request) {
        User usuario = authService.obterUsuarioAutenticado();

        String icone = request.getIcone();
        String cor = request.getCor();
        if (icone == null || icone.isBlank()) {
            icone = definirIconePadrao(request.getEsporte());
        }
        if (cor == null || cor.isBlank()) {
            cor = definirCorPadrao(request.getEsporte());
        }

        PreferenciaEsporte pref = PreferenciaEsporte.builder()
                .esporte(request.getEsporte().toUpperCase(Locale.ROOT))
                .nomeInteresse(request.getNomeInteresse().trim())
                .icone(icone)
                .cor(cor)
                .ativo(true)
                .usuario(usuario)
                .build();

        return preferenciaRepository.save(pref);
    }

    @Transactional
    public void removerPreferencia(Long id) {
        User usuario = authService.obterUsuarioAutenticado();
        PreferenciaEsporte pref = preferenciaRepository.findByIdAndUsuarioAndAtivoTrue(id, usuario)
                .orElseThrow(() -> new ResourceNotFoundException("PreferenciaEsporte", id));
        pref.setAtivo(false);
        preferenciaRepository.save(pref);
    }

    // --- Catálogo Multi-Esportes & Eventos ---
    @Transactional(readOnly = true)
    public List<EsporteEventoResponse> obterEventos(String esporteFiltro, String busca) {
        User usuario = authService.obterUsuarioAutenticado();
        List<PreferenciaEsporte> prefs = preferenciaRepository.findByUsuarioAndAtivoTrue(usuario);
        List<EsporteEventoResponse> todosEventos = carregarCatalogo();

        LocalDate inicioMes = LocalDate.now().minusDays(7);
        LocalDate fimMes = LocalDate.now().plusMonths(3);
        List<EventoCalendario> eventosNoCalendario = eventoCalendarioRepository
                .findByUsuarioAndAtivoTrueAndDataEventoBetweenOrderByDataEventoAscHoraInicioAsc(usuario, inicioMes, fimMes);

        for (EsporteEventoResponse ev : todosEventos) {
            Optional<EventoCalendario> match = eventosNoCalendario.stream()
                    .filter(c -> c.getTitulo().contains(ev.getTitulo()) || ev.getTitulo().contains(c.getTitulo()))
                    .findFirst();
            if (match.isPresent()) {
                ev.setNoCalendario(true);
                ev.setEventoCalendarioId(match.get().getId());
            } else {
                ev.setNoCalendario(false);
            }
        }

        if (esporteFiltro != null && !esporteFiltro.isBlank() && !"ALL".equalsIgnoreCase(esporteFiltro)) {
            String esp = esporteFiltro.trim().toUpperCase(Locale.ROOT);
            todosEventos = todosEventos.stream()
                    .filter(e -> e.getEsporte().equalsIgnoreCase(esp))
                    .collect(Collectors.toList());
        }

        if (busca != null && !busca.isBlank()) {
            String termo = busca.trim().toLowerCase(Locale.ROOT);
            return todosEventos.stream()
                    .filter(e -> e.getTitulo().toLowerCase(Locale.ROOT).contains(termo) ||
                                 e.getSubtitulo().toLowerCase(Locale.ROOT).contains(termo) ||
                                 e.getEsporte().toLowerCase(Locale.ROOT).contains(termo))
                    .collect(Collectors.toList());
        }

        if (!prefs.isEmpty() && (esporteFiltro == null || "ALL".equalsIgnoreCase(esporteFiltro))) {
            List<String> interesses = prefs.stream()
                    .map(p -> p.getNomeInteresse().toLowerCase(Locale.ROOT))
                    .collect(Collectors.toList());
            List<String> esportesSeguidos = prefs.stream()
                    .map(p -> p.getEsporte().toLowerCase(Locale.ROOT))
                    .collect(Collectors.toList());

            todosEventos.sort((a, b) -> {
                boolean aMatch = esportesSeguidos.contains(a.getEsporte().toLowerCase(Locale.ROOT)) ||
                        interesses.stream().anyMatch(i -> a.getTitulo().toLowerCase(Locale.ROOT).contains(i));
                boolean bMatch = esportesSeguidos.contains(b.getEsporte().toLowerCase(Locale.ROOT)) ||
                        interesses.stream().anyMatch(i -> b.getTitulo().toLowerCase(Locale.ROOT).contains(i));
                return Boolean.compare(bMatch, aMatch);
            });
        }

        return todosEventos;
    }

    @Transactional
    public EsporteEventoResponse salvarNoCalendario(String eventoId) {
        User usuario = authService.obterUsuarioAutenticado();
        EsporteEventoResponse evento = buscarEventoPorId(eventoId);

        String tituloCalendario = String.format("[%s %s] %s", evento.getIcone(), evento.getEsporte(), evento.getTitulo());
        String descricao = String.format("%s | Transmissão: %s", evento.getSubtitulo(), evento.getTransmissao());

        Optional<EventoCalendario> existente = eventoCalendarioRepository
                .findByUsuarioAndAtivoTrueAndDataEventoBetweenOrderByDataEventoAscHoraInicioAsc(
                        usuario, evento.getData(), evento.getData()
                ).stream()
                .filter(e -> e.getTitulo().equals(tituloCalendario))
                .findFirst();

        EventoCalendario eventoSalvo;
        if (existente.isPresent()) {
            eventoSalvo = existente.get();
        } else {
            EventoCalendario novo = EventoCalendario.builder()
                    .titulo(tituloCalendario)
                    .descricao(descricao)
                    .dataEvento(evento.getData())
                    .horaInicio(evento.getHora())
                    .cor(definirCorPadrao(evento.getEsporte()))
                    .categoria("Esportes")
                    .ativo(true)
                    .usuario(usuario)
                    .build();
            eventoSalvo = eventoCalendarioRepository.save(novo);
        }

        evento.setNoCalendario(true);
        evento.setEventoCalendarioId(eventoSalvo.getId());
        return evento;
    }

    @Transactional
    public EsporteEventoResponse removerDoCalendario(String eventoId) {
        User usuario = authService.obterUsuarioAutenticado();
        EsporteEventoResponse evento = buscarEventoPorId(eventoId);

        String prefixo = String.format("[%s %s]", evento.getIcone(), evento.getEsporte());
        List<EventoCalendario> eventos = eventoCalendarioRepository
                .findByUsuarioAndAtivoTrueAndDataEventoBetweenOrderByDataEventoAscHoraInicioAsc(
                        usuario, evento.getData(), evento.getData()
                );

        for (EventoCalendario ev : eventos) {
            if (ev.getTitulo().contains(evento.getTitulo()) || ev.getTitulo().contains(prefixo)) {
                ev.setAtivo(false);
                eventoCalendarioRepository.save(ev);
            }
        }

        evento.setNoCalendario(false);
        evento.setEventoCalendarioId(null);
        return evento;
    }

    public List<EsporteJogoResponse> obterJogosDoDia(String timeFiltro) {
        return obterEventos("FUTEBOL", timeFiltro).stream()
                .map(e -> {
                    String[] partes = e.getTitulo().split(" vs ");
                    if (partes.length == 1) {
                        partes = e.getTitulo().split(" x ");
                    }
                    String mandante = partes.length > 0 ? partes[0] : e.getTitulo();
                    String visitante = partes.length > 1 ? partes[1] : "";
                    return EsporteJogoResponse.builder()
                            .timeCasa(mandante)
                            .timeVisitante(visitante)
                            .horario(e.getDataHoraFormatada())
                            .campeonato(e.getSubtitulo())
                            .status(e.getStatus())
                            .estadio(e.getTransmissao())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Alertas de jogos que começam em até 1h (eventos de Esportes no calendário).
     * Marca como alertados para não repetir.
     */
    @Transactional
    public List<com.todolist.dto.AlertaEsporteResponse> coletarAlertasProximos() {
        User usuario = authService.obterUsuarioAutenticado();
        LocalDate hoje = LocalDate.now();
        LocalTime agora = LocalTime.now();
        List<EventoCalendario> candidatos = eventoCalendarioRepository
                .findEsportesSemAlertaHoje(usuario, hoje);

        List<com.todolist.dto.AlertaEsporteResponse> alertas = new ArrayList<>();
        for (EventoCalendario ev : candidatos) {
            if (ev.getHoraInicio() == null) {
                continue;
            }
            long minutos = java.time.Duration.between(agora, ev.getHoraInicio()).toMinutes();
            if (minutos < 0 || minutos > 60) {
                continue;
            }
            alertas.add(com.todolist.dto.AlertaEsporteResponse.builder()
                    .eventoId(ev.getId())
                    .titulo(ev.getTitulo())
                    .descricao(ev.getDescricao())
                    .data(ev.getDataEvento())
                    .hora(ev.getHoraInicio())
                    .minutosParaInicio(minutos)
                    .tipo("ESPORTE_BREVE")
                    .mensagem(minutos <= 5
                            ? ("Começa agora: " + ev.getTitulo())
                            : ("Em " + minutos + " min: " + ev.getTitulo()))
                    .build());
            ev.setAlertaEnviado(true);
            eventoCalendarioRepository.save(ev);
        }
        return alertas;
    }

    /** Preferências de demonstração / seed. */
    private List<EsporteEventoResponse> carregarCatalogo() {
        try {
            List<TheSportsDbDtos.Event> remotos = sportsDbClient.buscarCatalogo(LocalDate.now());
            if (remotos != null && !remotos.isEmpty()) {
                List<EsporteEventoResponse> filtrados = eventMapper.toResponseList(remotos).stream()
                        .filter(this::ligaPermitida)
                        .collect(Collectors.toList());
                if (!filtrados.isEmpty()) {
                    return new ArrayList<>(filtrados);
                }
            }
            log.info("TheSportsDB nao retornou eventos das ligas permitidas; usando catalogo de demonstracao.");
        } catch (Exception ex) {
            log.warn("Falha ao consultar TheSportsDB, usando catalogo de demonstracao: {}", ex.getMessage());
        }
        return gerarCatalogoDemo();
    }

    /**
     * Radar curado: so Premier League, La Liga, Serie A, Bundesliga, Ligue 1,
     * Champions, Brasileirao, Libertadores, F1, NBA, NFL e UFC.
     */
    private boolean ligaPermitida(EsporteEventoResponse ev) {
        String liga = ev.getSubtitulo() == null ? "" : ev.getSubtitulo().toLowerCase(Locale.ROOT);
        String titulo = ev.getTitulo() == null ? "" : ev.getTitulo().toLowerCase(Locale.ROOT);
        String esporte = ev.getEsporte() == null ? "" : ev.getEsporte().toUpperCase(Locale.ROOT);

        // IDs de demo sempre passam (fallback offline)
        if (ev.getId() != null && ev.getId().startsWith("DEMO-")) {
            return true;
        }

        for (String bloqueada : props.getLigasBloqueadasNomes()) {
            if (bloqueada != null && !bloqueada.isBlank()
                    && (liga.contains(bloqueada.toLowerCase(Locale.ROOT))
                    || titulo.contains(bloqueada.toLowerCase(Locale.ROOT)))) {
                return false;
            }
        }

        // UFC estrito: ignora AEW/WWE/PFL (ja bloqueadas acima) e exige UFC no rotulo
        if ("UFC".equals(esporte)) {
            return liga.contains("ufc") || titulo.contains("ufc");
        }
        if ("F1".equals(esporte)) {
            return liga.contains("formula 1") || liga.contains("f1") || titulo.contains("formula") || titulo.contains("grand prix");
        }
        if ("BASQUETE".equals(esporte)) {
            return liga.contains("nba");
        }
        if ("NFL".equals(esporte)) {
            return liga.contains("nfl") || titulo.contains("nfl");
        }
        if ("FUTEBOL".equals(esporte)) {
            for (String ok : List.of(
                    "premier league", "la liga", "serie a", "bundesliga", "ligue 1",
                    "champions league", "brasileirao", "brasileirão", "brazilian serie a",
                    "libertadores")) {
                if (liga.contains(ok)) {
                    return true;
                }
            }
            // Serie A italiana vs outras "Serie A": so aceita se for italiana/internacional top
            if (liga.contains("serie a") && (liga.contains("italian") || liga.contains("italia") || liga.contains("italy"))) {
                return true;
            }
            return false;
        }

        // Demais modalidades: bloqueia por padrao
        return false;
    }

    private EsporteEventoResponse buscarEventoPorId(String eventoId) {
        if (eventoId == null || eventoId.isBlank()) {
            throw new ResourceNotFoundException("EventoEsportivo", 0L);
        }

        if (eventoId.startsWith("tsdb-")) {
            String idExterno = eventoId.substring("tsdb-".length());
            TheSportsDbDtos.Event remoto = sportsDbClient.buscarEventoPorId(idExterno);
            if (remoto != null) {
                return eventMapper.toResponse(remoto);
            }
        }

        return carregarCatalogo().stream()
                .filter(e -> e.getId().equalsIgnoreCase(eventoId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("EventoEsportivo", 0L));
    }

    private List<EsporteEventoResponse> gerarCatalogoDemo() {
        LocalDate hoje = LocalDate.now();
        List<EsporteEventoResponse> eventos = new ArrayList<>();

        eventos.add(EsporteEventoResponse.builder()
                .id("DEMO-UFC-1")
                .esporte("UFC")
                .icone("🥊")
                .titulo("Card de demonstração: luta principal")
                .subtitulo("Catálogo offline — configure a TheSportsDB ou aguarde a API")
                .data(hoje.plusDays(3))
                .hora(LocalTime.of(22, 0))
                .dataHoraFormatada(formatarDataHora(hoje.plusDays(3), LocalTime.of(22, 0)))
                .transmissao("Indisponível offline")
                .status("AGENDADO")
                .resultado(null)
                .build());

        eventos.add(EsporteEventoResponse.builder()
                .id("DEMO-FUT-1")
                .esporte("FUTEBOL")
                .icone("⚽")
                .titulo("Amistoso de demonstração")
                .subtitulo("Catálogo offline")
                .data(hoje.plusDays(1))
                .hora(LocalTime.of(16, 0))
                .dataHoraFormatada(formatarDataHora(hoje.plusDays(1), LocalTime.of(16, 0)))
                .transmissao("Indisponível offline")
                .status("AGENDADO")
                .resultado(null)
                .build());

        return eventos;
    }

    private String formatarDataHora(LocalDate data, LocalTime hora) {
        DateTimeFormatter dFmt = DateTimeFormatter.ofPattern("dd/MM");
        DateTimeFormatter hFmt = DateTimeFormatter.ofPattern("HH:mm");
        return String.format("%s às %s", data.format(dFmt), hora.format(hFmt));
    }

    private String definirIconePadrao(String esporte) {
        if (esporte == null) return "⚽";
        return switch (esporte.toUpperCase(Locale.ROOT)) {
            case "UFC", "MMA", "BOXE" -> "🥊";
            case "F1", "FORMULA 1", "AUTOMOBILISMO" -> "🏎️";
            case "BASQUETE", "NBA", "NBB" -> "🏀";
            case "NFL", "FUTEBOL AMERICANO" -> "🏈";
            case "TENIS" -> "🎾";
            default -> "⚽";
        };
    }

    private String definirCorPadrao(String esporte) {
        if (esporte == null) return "#10b981";
        return switch (esporte.toUpperCase(Locale.ROOT)) {
            case "UFC", "MMA" -> "#ef4444";
            case "F1", "FORMULA 1" -> "#dc2626";
            case "BASQUETE", "NBA" -> "#f59e0b";
            case "NFL" -> "#3b82f6";
            case "TENIS" -> "#84cc16";
            default -> "#10b981";
        };
    }
}
