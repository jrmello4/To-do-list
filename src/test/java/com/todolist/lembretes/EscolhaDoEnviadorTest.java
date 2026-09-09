package com.todolist.lembretes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Sem servidor de e-mail configurado, quem tem que estar ligado é o enviador
 * que escreve no log.
 *
 * O caso parece bobo até alguém deixar spring.mail.host definido como string
 * vazia: aí o enviador de verdade sobe sem servidor nenhum e cada lembrete
 * vira uma exceção na varredura.
 */
@SpringBootTest
@DisplayName("Escolha do enviador")
class EscolhaDoEnviadorTest {

    @Autowired
    private EnviadorDeLembrete enviador;

    @Test
    @DisplayName("sem spring.mail.host, o enviador ativo é o que só registra no log")
    void semServidorDeEmail() {
        assertThat(enviador).isInstanceOf(EnviadorEmLog.class);
    }
}
