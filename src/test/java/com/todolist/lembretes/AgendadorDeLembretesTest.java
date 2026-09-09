package com.todolist.lembretes;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * O agendador é a única peça que ninguém percebe faltando.
 *
 * Se o nome da propriedade estiver errado, tudo continua compilando e passando:
 * a varredura simplesmente nunca roda, e o sintoma é um e-mail que não chega —
 * meses depois, se alguém reparar.
 */
@DisplayName("Agendador de lembretes")
class AgendadorDeLembretesTest {

    @Nested
    @SpringBootTest
    @DisplayName("sem LEMBRETES_ATIVOS")
    class Desligado {

        @Autowired
        private ApplicationContext contexto;

        @Test
        @DisplayName("o agendador não existe, e nada é enviado sozinho")
        void naoRegistra() {
            assertThat(contexto.getBeanNamesForType(AgendadorDeLembretes.class)).isEmpty();
        }
    }

    @Nested
    @SpringBootTest(properties = "LEMBRETES_ATIVOS=true")
    @DisplayName("com LEMBRETES_ATIVOS=true")
    class Ligado {

        @Autowired
        private ApplicationContext contexto;

        @Test
        @DisplayName("o agendador sobe")
        void registra() {
            assertThat(contexto.getBeanNamesForType(AgendadorDeLembretes.class)).hasSize(1);
        }
    }
}
