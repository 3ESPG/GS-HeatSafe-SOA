package br.com.fiap3espg.heatsafe.domain.risco;

import lombok.Getter;

/**
 * Faixas de risco do índice de calor (escala NOAA), as mesmas usadas no dispositivo ESP32,
 * no app e no modelo de ML do HeatSafe.
 */
@Getter
public enum NivelRisco {
    NORMAL(Double.NEGATIVE_INFINITY, 27, "Normal", "Temperatura tranquila. Continue bebendo água ao longo do dia."),
    ATENCAO(27, 32, "Atenção", "Está esquentando. Beba água com frequência."),
    CAUTELA_EXTREMA(32, 41, "Cautela extrema", "Calor forte. Beba água, abra as janelas e fique no cômodo mais fresco."),
    PERIGO(41, 54, "Perigo", "Calor perigoso. Risco de exaustão: hidratação e acompanhamento de idosos."),
    PERIGO_EXTREMO(54, Double.POSITIVE_INFINITY, "Perigo extremo", "Risco grave à saúde. Em caso de tontura ou confusão, ligue 192.");

    /** A partir deste índice de calor (°C) o dia é considerado perigoso no HeatSafe. */
    public static final double LIMIAR_DIA_PERIGOSO = 32.0;

    private final double minimo;
    private final double maximo;
    private final String rotulo;
    private final String orientacao;

    NivelRisco(double minimo, double maximo, String rotulo, String orientacao) {
        this.minimo = minimo;
        this.maximo = maximo;
        this.rotulo = rotulo;
        this.orientacao = orientacao;
    }

    public static NivelRisco de(double indiceCalor) {
        for (NivelRisco nivel : values()) {
            if (indiceCalor >= nivel.minimo && indiceCalor < nivel.maximo) {
                return nivel;
            }
        }
        throw new IllegalArgumentException("Índice de calor inválido: " + indiceCalor);
    }

    public boolean isPerigoso() {
        return this.compareTo(CAUTELA_EXTREMA) >= 0;
    }
}
