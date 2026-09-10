package com.todolist.service;

import com.todolist.dto.EsporteEventoResponse;
import com.todolist.dto.EsporteJogoResponse;
import com.todolist.dto.PreferenciaEsporteRequest;
import com.todolist.entity.EventoCalendario;
import com.todolist.entity.PreferenciaEsporte;
import com.todolist.entity.User;
import com.todolist.exception.ResourceNotFoundException;
import com.todolist.repository.EventoCalendarioRepository;
import com.todolist.repository.PreferenciaEsporteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EsporteService {

    private final PreferenciaEsporteRepository preferenciaRepository;
    private final EventoCalendarioRepository eventoCalendarioRepository;
    private final AuthService authService;

    // --- Preferências de Esportes do Usuário ---
    @Transactional
    public List<PreferenciaEsporte> obterPreferenciasDoUsuario() {
        User usuario = authService.obterUsuarioAutenticado();
        if (preferenciaRepository.countByUsuarioAndAtivoTrue(usuario) == 0) {
            inicializarPreferenciasPadrao(usuario);
        }
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
                .esporte(request.getEsporte().toUpperCase())
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
        List<EsporteEventoResponse> todosEventos = gerarCatalogoEventos();

        // Verifica quais eventos já estão no calendário do usuário
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

        // Filtro por esporte específico (ex: UFC, F1, FUTEBOL, BASQUETE)
        if (esporteFiltro != null && !esporteFiltro.isBlank() && !"ALL".equalsIgnoreCase(esporteFiltro)) {
            String esp = esporteFiltro.trim().toUpperCase();
            todosEventos = todosEventos.stream()
                    .filter(e -> e.getEsporte().equalsIgnoreCase(esp))
                    .collect(Collectors.toList());
        }

        // Filtro por termo de busca (time, lutador ou competição)
        if (busca != null && !busca.isBlank()) {
            String termo = busca.trim().toLowerCase();
            return todosEventos.stream()
                    .filter(e -> e.getTitulo().toLowerCase().contains(termo) ||
                                 e.getSubtitulo().toLowerCase().contains(termo) ||
                                 e.getEsporte().toLowerCase().contains(termo))
                    .collect(Collectors.toList());
        }

        // Se o usuário tem preferências cadastradas, prioriza ou filtra
        if (!prefs.isEmpty() && (esporteFiltro == null || "ALL".equalsIgnoreCase(esporteFiltro))) {
            List<String> interesses = prefs.stream()
                    .map(p -> p.getNomeInteresse().toLowerCase())
                    .collect(Collectors.toList());
            List<String> esportesSeguidos = prefs.stream()
                    .map(p -> p.getEsporte().toLowerCase())
                    .collect(Collectors.toList());

            // Ordena eventos: os que batem com os interesses do usuário primeiro
            todosEventos.sort((a, b) -> {
                boolean aMatch = esportesSeguidos.contains(a.getEsporte().toLowerCase()) ||
                        interesses.stream().anyMatch(i -> a.getTitulo().toLowerCase().contains(i));
                boolean bMatch = esportesSeguidos.contains(b.getEsporte().toLowerCase()) ||
                        interesses.stream().anyMatch(i -> b.getTitulo().toLowerCase().contains(i));
                return Boolean.compare(bMatch, aMatch);
            });
        }

        return todosEventos;
    }

    // --- Sincronização com o Calendário Unificado ---
    @Transactional
    public EsporteEventoResponse salvarNoCalendario(String eventoId) {
        User usuario = authService.obterUsuarioAutenticado();
        EsporteEventoResponse evento = gerarCatalogoEventos().stream()
                .filter(e -> e.getId().equalsIgnoreCase(eventoId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("EventoEsportivo", 0L));

        String tituloCalendario = String.format("[%s %s] %s", evento.getIcone(), evento.getEsporte(), evento.getTitulo());
        String descricao = String.format("%s | Transmissão: %s", evento.getSubtitulo(), evento.getTransmissao());

        // Verifica se já existe
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
        EsporteEventoResponse evento = gerarCatalogoEventos().stream()
                .filter(e -> e.getId().equalsIgnoreCase(eventoId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("EventoEsportivo", 0L));

        String prefixo = String.format("[%s %s]", evento.getIcone(), evento.getEsporte());
        List<EventoCalendario> eventos = eventoCalendarioRepository
                .findByUsuarioAndAtivoTrueAndDataEventoBetweenOrderByDataEventoAscHoraInicioAsc(
                        usuario, evento.getData(), evento.getData()
                );

        for (EventoCalendario ev : eventos) {
            if (ev.getTitulo().contains(evento.getTitulo())) {
                ev.setAtivo(false);
                eventoCalendarioRepository.save(ev);
            }
        }

        evento.setNoCalendario(false);
        evento.setEventoCalendarioId(null);
        return evento;
    }

    // Método de compatibilidade para endpoint legado
    public List<EsporteJogoResponse> obterJogosDoDia(String timeFiltro) {
        return obterEventos("FUTEBOL", timeFiltro).stream()
                .map(e -> {
                    String[] partes = e.getTitulo().split(" x ");
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

    private void inicializarPreferenciasPadrao(User usuario) {
        List<PreferenciaEsporte> padroes = List.of(
                PreferenciaEsporte.builder().esporte("UFC").nomeInteresse("UFC").icone("🥊").cor("#ef4444").ativo(true).usuario(usuario).build(),
                PreferenciaEsporte.builder().esporte("F1").nomeInteresse("Fórmula 1").icone("🏎️").cor("#dc2626").ativo(true).usuario(usuario).build(),
                PreferenciaEsporte.builder().esporte("FUTEBOL").nomeInteresse("Flamengo").icone("⚽").cor("#10b981").ativo(true).usuario(usuario).build(),
                PreferenciaEsporte.builder().esporte("BASQUETE").nomeInteresse("Lakers (NBA)").icone("🏀").cor("#f59e0b").ativo(true).usuario(usuario).build()
        );
        preferenciaRepository.saveAll(padroes);
    }

    private List<EsporteEventoResponse> gerarCatalogoEventos() {
        LocalDate hoje = LocalDate.now();
        List<EsporteEventoResponse> eventos = new ArrayList<>();

        // --- UFC / MMA ---
        eventos.add(EsporteEventoResponse.builder()
                .id("UFC-315")
                .esporte("UFC")
                .icone("🥊")
                .titulo("UFC 315: Makhachev vs. Oliveira 2")
                .subtitulo("Disputa de Cinturão Peso Leve • Card Principal")
                .data(hoje.plusDays(3))
                .hora(LocalTime.of(23, 0))
                .dataHoraFormatada(formatarDataHora(hoje.plusDays(3), LocalTime.of(23, 0)))
                .transmissao("📺 UFC Fight Pass, Band")
                .status("AGENDADO")
                .resultado(null)
                .build());

        eventos.add(EsporteEventoResponse.builder()
                .id("UFC-FN-POATAN")
                .esporte("UFC")
                .icone("🥊")
                .titulo("UFC Fight Night: Poatan vs. Ankalaev")
                .subtitulo("Defesa de Título Meio-Pesado • Luta Principal")
                .data(hoje.plusDays(10))
                .hora(LocalTime.of(22, 30))
                .dataHoraFormatada(formatarDataHora(hoje.plusDays(10), LocalTime.of(22, 30)))
                .transmissao("📺 UFC Fight Pass")
                .status("AGENDADO")
                .resultado(null)
                .build());

        // --- Fórmula 1 ---
        eventos.add(EsporteEventoResponse.builder()
                .id("F1-SP")
                .esporte("F1")
                .icone("🏎️")
                .titulo("GP de São Paulo (Interlagos) - Corrida")
                .subtitulo("Fórmula 1 • 71 voltas • Circuito de Interlagos")
                .data(hoje.plusDays(5))
                .hora(LocalTime.of(14, 0))
                .dataHoraFormatada(formatarDataHora(hoje.plusDays(5), LocalTime.of(14, 0)))
                .transmissao("📺 Band, Bandplay, F1 TV Pro")
                .status("AGENDADO")
                .resultado(null)
                .build());

        // --- Futebol ---
        eventos.add(EsporteEventoResponse.builder()
                .id("FUT-FLA-PAL")
                .esporte("FUTEBOL")
                .icone("⚽")
                .titulo("Flamengo x Palmeiras")
                .subtitulo("Brasileirão Série A • Maracanã • Rodada 28")
                .data(hoje)
                .hora(LocalTime.of(21, 30))
                .dataHoraFormatada("Hoje 21:30")
                .transmissao("📺 Globo, Premiere")
                .status("AO VIVO")
                .resultado("1 x 0 (68')")
                .build());

        eventos.add(EsporteEventoResponse.builder()
                .id("FUT-RMA-MCI")
                .esporte("FUTEBOL")
                .icone("⚽")
                .titulo("Real Madrid x Manchester City")
                .subtitulo("UEFA Champions League • Santiago Bernabéu")
                .data(hoje.plusDays(1))
                .hora(LocalTime.of(16, 0))
                .dataHoraFormatada(formatarDataHora(hoje.plusDays(1), LocalTime.of(16, 0)))
                .transmissao("📺 TNT, Max, SBT")
                .status("AGENDADO")
                .resultado(null)
                .build());

        eventos.add(EsporteEventoResponse.builder()
                .id("FUT-SAO-COR")
                .esporte("FUTEBOL")
                .icone("⚽")
                .titulo("São Paulo x Corinthians")
                .subtitulo("Brasileirão Série A • MorumBIS")
                .data(hoje.plusDays(2))
                .hora(LocalTime.of(18, 30))
                .dataHoraFormatada(formatarDataHora(hoje.plusDays(2), LocalTime.of(18, 30)))
                .transmissao("📺 Premiere")
                .status("AGENDADO")
                .resultado(null)
                .build());

        // --- Basquete (NBA) ---
        eventos.add(EsporteEventoResponse.builder()
                .id("NBA-LAL-GSW")
                .esporte("BASQUETE")
                .icone("🏀")
                .titulo("Los Angeles Lakers x Golden State Warriors")
                .subtitulo("NBA • Crypto.com Arena • Temporada Regular")
                .data(hoje.plusDays(1))
                .hora(LocalTime.of(23, 0))
                .dataHoraFormatada(formatarDataHora(hoje.plusDays(1), LocalTime.of(23, 0)))
                .transmissao("📺 ESPN, Disney+, NBA League Pass")
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
        return switch (esporte.toUpperCase()) {
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
        return switch (esporte.toUpperCase()) {
            case "UFC", "MMA" -> "#ef4444";
            case "F1", "FORMULA 1" -> "#dc2626";
            case "BASQUETE", "NBA" -> "#f59e0b";
            case "NFL" -> "#3b82f6";
            case "TENIS" -> "#84cc16";
            default -> "#10b981";
        };
    }
}
