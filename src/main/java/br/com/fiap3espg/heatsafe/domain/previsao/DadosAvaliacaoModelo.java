package br.com.fiap3espg.heatsafe.domain.previsao;

import java.time.LocalDate;

/**
 * Avaliação do modelo em um período (backtest): para cada dia, a previsão usa só o histórico anterior
 * e é comparada com o que realmente aconteceu no dia seguinte.
 */
public record DadosAvaliacaoModelo(
        String estacao,
        MetodoPrevisao metodo,
        LocalDate inicio,
        LocalDate fim,
        double limiarProbabilidade,
        int diasAvaliados,
        int verdadeirosPositivos,
        int falsosPositivos,
        int falsosNegativos,
        int verdadeirosNegativos,
        double recall,
        double precisao,
        double f1,
        double acuracia) {
}
