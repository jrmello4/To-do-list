package com.todolist.service.esportes;

import com.todolist.config.EsportesApiProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Cliente HTTP da API pública TheSportsDB (v1).
 * Limites free: ~30 req/min e poucos itens por endpoint — por isso cache + fallback.
 */
@Component
public class TheSportsDbClient {

    private static final Logger log = LoggerFactory.getLogger(TheSportsDbClient.class);
    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    private final RestClient restClient;
    private final EsportesApiProperties properties;

    public TheSportsDbClient(RestClient sportsDbRestClient, EsportesApiProperties properties) {
        this.restClient = sportsDbRestClient;
        this.properties = properties;
    }

    @Cacheable(cacheNames = "esportes", key = "'catalogo:' + #hoje")
    public List<TheSportsDbDtos.Event> buscarCatalogo(LocalDate hoje) {
        if (!properties.isEnabled()) {
            return List.of();
        }

        Map<String, TheSportsDbDtos.Event> unicos = new LinkedHashMap<>();

        for (String esporte : properties.getEsportesDia()) {
            for (int offset = 0; offset <= 2; offset++) {
                LocalDate data = hoje.plusDays(offset);
                List<TheSportsDbDtos.Event> eventos = buscarEventosDoDia(data, esporte);
                for (TheSportsDbDtos.Event e : eventos) {
                    if (e != null && e.getIdEvent() != null) {
                        unicos.putIfAbsent(e.getIdEvent(), e);
                    }
                }
            }
        }

        for (EsportesApiProperties.Liga liga : properties.getLigas()) {
            List<TheSportsDbDtos.Event> proximos = buscarProximosDaLiga(liga.getId());
            for (TheSportsDbDtos.Event e : proximos) {
                if (e != null && e.getIdEvent() != null) {
                    unicos.putIfAbsent(e.getIdEvent(), e);
                }
            }
        }

        return new ArrayList<>(unicos.values());
    }

    public TheSportsDbDtos.Event buscarEventoPorId(String idEvent) {
        if (!properties.isEnabled() || idEvent == null || idEvent.isBlank()) {
            return null;
        }
        try {
            TheSportsDbDtos.EventListResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .pathSegment("lookupevent.php")
                            .queryParam("id", idEvent)
                            .build())
                    .retrieve()
                    .body(TheSportsDbDtos.EventListResponse.class);
            if (response == null || response.getEvents() == null || response.getEvents().isEmpty()) {
                return null;
            }
            return response.getEvents().get(0);
        } catch (RestClientException ex) {
            log.warn("Falha ao buscar evento {} na TheSportsDB: {}", idEvent, ex.getMessage());
            return null;
        }
    }

    private List<TheSportsDbDtos.Event> buscarEventosDoDia(LocalDate data, String esporte) {
        try {
            TheSportsDbDtos.EventListResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .pathSegment("eventsday.php")
                            .queryParam("d", data.format(ISO_DATE))
                            .queryParam("s", esporte)
                            .build())
                    .retrieve()
                    .body(TheSportsDbDtos.EventListResponse.class);
            if (response == null || response.getEvents() == null) {
                return List.of();
            }
            return response.getEvents();
        } catch (RestClientException ex) {
            log.debug("eventsday ({}, {}) indisponível: {}", data, esporte, ex.getMessage());
            return List.of();
        }
    }

    private List<TheSportsDbDtos.Event> buscarProximosDaLiga(int idLiga) {
        try {
            TheSportsDbDtos.EventListResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .pathSegment("eventsnextleague.php")
                            .queryParam("id", idLiga)
                            .build())
                    .retrieve()
                    .body(TheSportsDbDtos.EventListResponse.class);
            if (response == null || response.getEvents() == null) {
                return List.of();
            }
            return response.getEvents();
        } catch (RestClientException ex) {
            log.debug("eventsnextleague ({}) indisponível: {}", idLiga, ex.getMessage());
            return List.of();
        }
    }
}
