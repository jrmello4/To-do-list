package com.todolist.lembretes;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * Dispara a varredura de hora em hora.
 *
 * Desligado por padrão: numa máquina de desenvolvimento ninguém quer um job
 * rodando de hora em hora, e num deploy com mais de uma instância todas
 * enviariam o mesmo lembrete. Ligue com LEMBRETES_ATIVOS=true numa instância só.
 */
@Component
@ConditionalOnProperty(name = "LEMBRETES_ATIVOS", havingValue = "true")
@RequiredArgsConstructor
public class AgendadorDeLembretes {

    private static final Logger log = LoggerFactory.getLogger(AgendadorDeLembretes.class);

    private final LembreteService lembreteService;

    @Scheduled(cron = "0 0 * * * *", zone = "UTC")
    public void aCadaHora() {
        int enviados = lembreteService.varrer(ZonedDateTime.now(ZoneOffset.UTC));

        if (enviados > 0) {
            log.info("Varredura de lembretes: {} enviados", enviados);
        }
    }
}
