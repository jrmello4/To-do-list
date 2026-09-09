package com.todolist.lembretes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Reserva para quando não há servidor de e-mail configurado.
 *
 * Registra o lembrete no log em vez de silenciosamente não fazer nada: sem
 * isso, alguém ligaria os lembretes em desenvolvimento e ficaria esperando
 * um e-mail que nunca foi tentado.
 */
@Component
@Primary
@ConditionalOnMissingBean(EnviadorPorEmail.class)
public class EnviadorEmLog implements EnviadorDeLembrete {

    private static final Logger log = LoggerFactory.getLogger(EnviadorEmLog.class);

    @Override
    public void enviar(Lembrete lembrete) {
        log.info("Lembrete NÃO enviado (spring.mail.host não configurado). Para {}: {}\n{}",
                lembrete.destinatario(), lembrete.assunto(), lembrete.corpo());
    }
}
