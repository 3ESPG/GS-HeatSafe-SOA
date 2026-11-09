package br.com.fiap3espg.heatsafe.domain.risco;

import br.com.fiap3espg.heatsafe.domain.ValidacaoException;

/**
 * Índice de calor (sensação térmica) da NOAA: regressão de Rothfusz com os ajustes oficiais.
 * Entrada e saída em graus Celsius. É a mesma fórmula do dispositivo ESP32 e da preparação dos dados do INMET.
 */
public final class CalculadoraIndiceCalor {

    private CalculadoraIndiceCalor() {
    }

    public static double calcular(double temperaturaCelsius, double umidadeRelativa) {
        if (umidadeRelativa < 0 || umidadeRelativa > 100) {
            throw new ValidacaoException("Umidade relativa deve estar entre 0 e 100%");
        }
        double t = temperaturaCelsius * 9 / 5 + 32;
        double rh = umidadeRelativa;
        double simples = 0.5 * (t + 61.0 + (t - 68.0) * 1.2 + rh * 0.094);
        if ((simples + t) / 2 < 80) {
            return arredondar(paraCelsius(simples));
        }

        double hi = -42.379 + 2.04901523 * t + 10.14333127 * rh - 0.22475541 * t * rh
                - 0.00683783 * t * t - 0.05481717 * rh * rh + 0.00122874 * t * t * rh
                + 0.00085282 * t * rh * rh - 0.00000199 * t * t * rh * rh;
        if (rh < 13 && t >= 80 && t <= 112) {
            hi -= ((13 - rh) / 4) * Math.sqrt((17 - Math.abs(t - 95)) / 17);
        } else if (rh > 85 && t >= 80 && t <= 87) {
            hi += ((rh - 85) / 10) * ((87 - t) / 5);
        }
        return arredondar(paraCelsius(hi));
    }

    private static double paraCelsius(double fahrenheit) {
        return (fahrenheit - 32) * 5 / 9;
    }

    private static double arredondar(double valor) {
        return Math.round(valor * 10) / 10.0;
    }
}
