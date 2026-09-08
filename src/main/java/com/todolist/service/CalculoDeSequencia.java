package com.todolist.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Set;

/**
 * Sequências derivadas dos registros, e não de um contador guardado.
 *
 * Um contador incrementado a cada marcação dessincroniza no primeiro registro
 * apagado ou marcado com atraso, e não há como perceber. Aqui a contagem é
 * recalculada a partir das linhas existentes toda vez.
 */
final class CalculoDeSequencia {

    /** Um ano para trás: o suficiente para qualquer sequência que interesse ver. */
    static final int JANELA_EM_DIAS = 365;

    private CalculoDeSequencia() {
    }

    /**
     * Dias consecutivos até hoje, contando só os dias em que o hábito vale.
     *
     * O dia de hoje é o único que ganha tolerância: ainda não acabou, então
     * não tê-lo cumprido não quebra a sequência. Qualquer dia anterior
     * aplicável e não cumprido quebra.
     */
    static int atual(Set<LocalDate> feitos, Set<DayOfWeek> dias, LocalDate hoje) {
        if (dias.isEmpty()) {
            return 0;
        }

        int sequencia = 0;
        LocalDate dia = hoje;

        for (int i = 0; i <= JANELA_EM_DIAS; i++) {
            if (dias.contains(dia.getDayOfWeek())) {
                if (feitos.contains(dia)) {
                    sequencia++;
                } else if (!dia.equals(hoje)) {
                    break;
                }
                // Se for hoje e não estiver feito, apenas segue para ontem.
            }
            dia = dia.minusDays(1);
        }

        return sequencia;
    }

    /** Maior sequência dentro da janela consultada. */
    static int maior(Set<LocalDate> feitos, Set<DayOfWeek> dias, LocalDate hoje) {
        if (feitos.isEmpty() || dias.isEmpty()) {
            return 0;
        }

        LocalDate inicio = feitos.stream().min(Comparator.naturalOrder()).orElse(hoje);
        int maior = 0;
        int corrente = 0;

        for (LocalDate dia = inicio; !dia.isAfter(hoje); dia = dia.plusDays(1)) {
            if (!dias.contains(dia.getDayOfWeek())) {
                continue;
            }

            if (feitos.contains(dia)) {
                corrente++;
                maior = Math.max(maior, corrente);
            } else if (!dia.equals(hoje)) {
                corrente = 0;
            }
        }

        return maior;
    }
}
