package com.todolist.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A regra de sequência é onde um contador guardado erraria. Estes testes fixam
 * os casos de borda: o dia de hoje ainda não acabou, e dias em que o hábito
 * não vale não podem contar como falha.
 */
@DisplayName("Cálculo de sequência de hábitos")
class CalculoDeSequenciaTest {

    /** Uma quarta-feira, para os casos com dias específicos da semana. */
    private static final LocalDate QUARTA = LocalDate.of(2026, 9, 9);

    private static final Set<DayOfWeek> TODOS_OS_DIAS =
            Arrays.stream(DayOfWeek.values()).collect(Collectors.toCollection(LinkedHashSet::new));

    private static final Set<DayOfWeek> SEG_QUA_SEX =
            new LinkedHashSet<>(Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY));

    private static Set<LocalDate> dias(LocalDate base, int... deslocamentos) {
        return Arrays.stream(deslocamentos).mapToObj(base::minusDays)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Nested
    @DisplayName("Sequência atual")
    class Atual {

        @Test
        @DisplayName("sem registro algum é zero")
        void semRegistros() {
            assertThat(CalculoDeSequencia.atual(Set.of(), TODOS_OS_DIAS, QUARTA)).isZero();
        }

        @Test
        @DisplayName("conta os dias consecutivos até hoje")
        void diasConsecutivos() {
            assertThat(CalculoDeSequencia.atual(dias(QUARTA, 0, 1, 2), TODOS_OS_DIAS, QUARTA))
                    .isEqualTo(3);
        }

        @Test
        @DisplayName("hoje não cumprido ainda não quebra: o dia não acabou")
        void hojeNaoQuebra() {
            // ontem e anteontem feitos, hoje ainda não
            assertThat(CalculoDeSequencia.atual(dias(QUARTA, 1, 2), TODOS_OS_DIAS, QUARTA))
                    .isEqualTo(2);
        }

        @Test
        @DisplayName("um dia anterior perdido quebra a sequência")
        void diaPerdidoQuebra() {
            // hoje e anteontem feitos, ontem não
            assertThat(CalculoDeSequencia.atual(dias(QUARTA, 0, 2), TODOS_OS_DIAS, QUARTA))
                    .isEqualTo(1);
        }

        @Test
        @DisplayName("dias em que o hábito não vale são pulados, não contam como falha")
        void diasNaoAplicaveisSaoPulados() {
            // Hábito de segunda, quarta e sexta. Feito na quarta (hoje), na
            // segunda e na sexta anterior. Terça e quinta não valem.
            Set<LocalDate> feitos = dias(QUARTA, 0, 2, 5);

            assertThat(CalculoDeSequencia.atual(feitos, SEG_QUA_SEX, QUARTA)).isEqualTo(3);
        }

        @Test
        @DisplayName("a tolerância vale só para hoje, não para o dia aplicável anterior")
        void toleranciaSoParaHoje() {
            // Hoje é quinta (o hábito não vale). A quarta valia e foi perdida:
            // a sequência está quebrada, mesmo hoje não sendo aplicável.
            LocalDate quinta = QUARTA.plusDays(1);
            Set<LocalDate> feitos = dias(quinta, 3);   // só a segunda

            assertThat(CalculoDeSequencia.atual(feitos, SEG_QUA_SEX, quinta)).isZero();
        }
    }

    @Nested
    @DisplayName("Maior sequência")
    class Maior {

        @Test
        @DisplayName("sem registro algum é zero")
        void semRegistros() {
            assertThat(CalculoDeSequencia.maior(Set.of(), TODOS_OS_DIAS, QUARTA)).isZero();
        }

        @Test
        @DisplayName("encontra o trecho mais longo, mesmo já interrompido")
        void trechoMaisLongoNoPassado() {
            // 4 dias seguidos há duas semanas, e 2 dias agora
            Set<LocalDate> feitos = dias(QUARTA, 0, 1, 14, 15, 16, 17);

            assertThat(CalculoDeSequencia.maior(feitos, TODOS_OS_DIAS, QUARTA)).isEqualTo(4);
            assertThat(CalculoDeSequencia.atual(feitos, TODOS_OS_DIAS, QUARTA)).isEqualTo(2);
        }

        @Test
        @DisplayName("é pelo menos igual à atual")
        void nuncaMenorQueAAtual() {
            Set<LocalDate> feitos = dias(QUARTA, 0, 1, 2, 3);

            assertThat(CalculoDeSequencia.maior(feitos, TODOS_OS_DIAS, QUARTA))
                    .isGreaterThanOrEqualTo(CalculoDeSequencia.atual(feitos, TODOS_OS_DIAS, QUARTA));
        }
    }
}
