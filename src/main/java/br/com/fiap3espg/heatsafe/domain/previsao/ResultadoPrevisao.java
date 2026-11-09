package br.com.fiap3espg.heatsafe.domain.previsao;

/** Saída de um modelo de previsão para o dia seguinte. */
public record ResultadoPrevisao(double indiceCalorPrevisto, double probabilidadePerigo, int amostrasHistoricas) {
}
