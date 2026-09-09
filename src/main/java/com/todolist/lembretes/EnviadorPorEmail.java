package com.todolist.lembretes;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/** Ativo apenas quando spring.mail.host está configurado. */
@Component
@ConditionalOnProperty(name = "spring.mail.host")
@RequiredArgsConstructor
public class EnviadorPorEmail implements EnviadorDeLembrete {

    private static final Logger log = LoggerFactory.getLogger(EnviadorPorEmail.class);

    private final JavaMailSender remetente;

    @Value("${LEMBRETE_REMETENTE:nao-responda@todolist.local}")
    private String de;

    @Override
    public void enviar(Lembrete lembrete) {
        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(de);
        mensagem.setTo(lembrete.destinatario());
        mensagem.setSubject("To-do List · " + lembrete.assunto());
        mensagem.setText(lembrete.corpo());

        remetente.send(mensagem);
        log.info("Lembrete enviado para {} ({} tarefas)", lembrete.destinatario(), lembrete.total());
    }
}
