package com.todolist.service.esportes;

import com.todolist.dto.EsporteEventoResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Mapeia eventos da TheSportsDB para o DTO interno do LifeHub.
 */
@Component
public class EsporteEventMapper {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATA_CURTA = DateTimeFormatter.ofPattern("dd/MM");

    public EsporteEventoResponse toResponse(TheSportsDbDtos.Event event) {
        LocalDate data = parseData(event.getDateEvent(), event.getStrTimestamp());
        LocalTime hora = parseHora(event.getStrTime(), event.getStrTimestamp());
        String esporte = normalizarEsporte(event.getStrSport());
        String icone = iconePorEsporte(esporte);
        String titulo = event.getStrEvent() != null ? event.getStrEvent() : "Evento";
        String subtitulo = montarSubtitulo(event);
        String resultado = montarResultado(event);
        String status = mapearStatus(event.getStrStatus(), resultado);

        return EsporteEventoResponse.builder()
                .id("tsdb-" + event.getIdEvent())
                .esporte(esporte)
                .icone(icone)
                .titulo(titulo)
                .subtitulo(subtitulo)
                .data(data)
                .hora(hora)
                .dataHoraFormatada(formatarDataHora(data, hora))
                .transmissao(event.getStrVenue() != null ? "Local: " + event.getStrVenue() : "Consulte emissora local")
                .status(status)
                .resultado(resultado)
                .build();
    }

    public List<EsporteEventoResponse> toResponseList(List<TheSportsDbDtos.Event> events) {
        List<EsporteEventoResponse> lista = new ArrayList<>();
        if (events == null) {
            return lista;
        }
        for (TheSportsDbDtos.Event e : events) {
            lista.add(toResponse(e));
        }
        return lista;
    }

    private LocalDate parseData(String dateEvent, String timestamp) {
        try {
            if (dateEvent != null && !dateEvent.isBlank()) {
                return LocalDate.parse(dateEvent);
            }
        } catch (Exception ignored) {
            // tenta timestamp
        }
        try {
            if (timestamp != null && timestamp.length() >= 10) {
                return LocalDate.parse(timestamp.substring(0, 10));
            }
        } catch (Exception ignored) {
            // fallback
        }
        return LocalDate.now();
    }

    private LocalTime parseHora(String strTime, String timestamp) {
        try {
            if (strTime != null && strTime.length() >= 5) {
                return LocalTime.parse(strTime.substring(0, 5));
            }
        } catch (Exception ignored) {
            // tenta timestamp
        }
        try {
            if (timestamp != null && timestamp.length() >= 16) {
                return LocalTime.parse(timestamp.substring(11, 16));
            }
        } catch (Exception ignored) {
            // fallback
        }
        return LocalTime.of(0, 0);
    }

    private String montarSubtitulo(TheSportsDbDtos.Event event) {
        StringBuilder sb = new StringBuilder();
        if (event.getStrLeague() != null && !event.getStrLeague().isBlank()) {
            sb.append(event.getStrLeague());
        }
        if (event.getStrHomeTeam() != null && event.getStrAwayTeam() != null) {
            if (!sb.isEmpty()) {
                sb.append(" • ");
            }
            sb.append(event.getStrHomeTeam()).append(" vs ").append(event.getStrAwayTeam());
        }
        return sb.isEmpty() ? "Evento esportivo" : sb.toString();
    }

    private String montarResultado(TheSportsDbDtos.Event event) {
        if (event.getIntHomeScore() == null || event.getIntAwayScore() == null
                || event.getIntHomeScore().isBlank() || event.getIntAwayScore().isBlank()) {
            return null;
        }
        return event.getIntHomeScore() + " x " + event.getIntAwayScore();
    }

    private String mapearStatus(String strStatus, String resultado) {
        if (strStatus == null) {
            return resultado != null ? "ENCERRADO" : "AGENDADO";
        }
        return switch (strStatus.toUpperCase(Locale.ROOT)) {
            case "FT", "AET", "PEN", "AWD", "ABD" -> "ENCERRADO";
            case "1H", "2H", "HT", "ET", "P", "LIVE", "NS" ->
                    ("1H".equalsIgnoreCase(strStatus) || "2H".equalsIgnoreCase(strStatus)
                            || "HT".equalsIgnoreCase(strStatus) || "ET".equalsIgnoreCase(strStatus)
                            || "P".equalsIgnoreCase(strStatus) || "LIVE".equalsIgnoreCase(strStatus))
                            ? "AO VIVO" : "AGENDADO";
            default -> resultado != null ? "ENCERRADO" : "AGENDADO";
        };
    }

    private String normalizarEsporte(String sport) {
        if (sport == null || sport.isBlank()) {
            return "GERAL";
        }
        String s = sport.trim().toUpperCase(Locale.ROOT);
        return switch (s) {
            case "SOCCER" -> "FUTEBOL";
            case "BASKETBALL" -> "BASQUETE";
            case "AMERICAN FOOTBALL" -> "NFL";
            case "FIGHTING", "MMA" -> "UFC";
            case "MOTORSPORT", "RACING" -> "F1";
            case "BASEBALL" -> "BEISEBOL";
            case "ICE HOCKEY" -> "HOCKEY";
            default -> s;
        };
    }

    private String iconePorEsporte(String esporte) {
        return switch (esporte) {
            case "UFC", "MMA", "BOXE" -> "🥊";
            case "F1", "FORMULA 1", "MOTORSPORT" -> "🏎️";
            case "BASQUETE", "NBA" -> "🏀";
            case "NFL" -> "🏈";
            case "BEISEBOL" -> "⚾";
            case "TENIS" -> "🎾";
            case "HOCKEY" -> "🏒";
            default -> "⚽";
        };
    }

    private String formatarDataHora(LocalDate data, LocalTime hora) {
        return String.format("%s às %s", data.format(DATA_CURTA), hora.format(HORA));
    }
}
