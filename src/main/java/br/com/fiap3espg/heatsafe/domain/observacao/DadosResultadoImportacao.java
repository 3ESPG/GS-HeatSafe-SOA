package br.com.fiap3espg.heatsafe.domain.observacao;

import java.time.LocalDate;

public record DadosResultadoImportacao(
        String estacao,
        int linhasLidas,
        int importadas,
        int ignoradasJaExistentes,
        int ignoradasIncompletas,
        LocalDate primeiraData,
        LocalDate ultimaData) {
}
