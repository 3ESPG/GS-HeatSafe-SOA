package br.com.fiap3espg.heatsafe.domain.previsao;

public enum MetodoPrevisao {
    /** Probabilidade calculada a partir de dias parecidos no histórico real do INMET. */
    FREQUENCIA_HISTORICA,
    /** Reservado para o modelo XGBoost treinado na disciplina de IA/ML. */
    MODELO_ML
}
