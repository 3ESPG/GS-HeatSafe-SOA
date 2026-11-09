package br.com.fiap3espg.heatsafe.domain.previsao;

import br.com.fiap3espg.heatsafe.domain.ValidacaoException;
import br.com.fiap3espg.heatsafe.domain.observacao.ObservacaoClimatica;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo estatístico de "dias análogos":
 * procura no histórico os dias parecidos com o dia base (índice de calor próximo e mesma época do ano)
 * e verifica o que aconteceu no dia seguinte a cada um deles.
 * A probabilidade de perigo é a fração desses "dias seguintes" que passaram de 32 °C de índice de calor.
 */
@Component
public class PrevisaoPorFrequenciaHistorica implements ModeloPrevisao {

    static final double TOLERANCIA_INDICE = 1.5;
    static final double TOLERANCIA_INDICE_AMPLIADA = 3.0;
    static final int DISTANCIA_MESES = 1;
    static final int AMOSTRAS_DESEJADAS = 20;
    static final int AMOSTRAS_MINIMAS = 5;

    @Override
    public MetodoPrevisao metodo() {
        return MetodoPrevisao.FREQUENCIA_HISTORICA;
    }

    @Override
    public ResultadoPrevisao prever(ObservacaoClimatica diaBase, List<ObservacaoClimatica> historico) {
        List<ParDias> pares = montarPares(historico, diaBase.getData());

        List<ParDias> analogos = filtrar(pares, diaBase, TOLERANCIA_INDICE, true);
        if (analogos.size() < AMOSTRAS_DESEJADAS) {
            analogos = filtrar(pares, diaBase, TOLERANCIA_INDICE_AMPLIADA, false);
        }
        if (analogos.size() < AMOSTRAS_MINIMAS) {
            throw new ValidacaoException("Histórico insuficiente para prever: são necessários ao menos "
                    + AMOSTRAS_MINIMAS + " dias parecidos com o dia base, encontrados " + analogos.size() + ".");
        }

        long perigosos = analogos.stream().filter(p -> p.seguinte().isDiaPerigoso()).count();
        double mediaIndiceSeguinte = analogos.stream()
                .mapToDouble(p -> p.seguinte().getIndiceCalorMaximo())
                .average()
                .orElseThrow();

        return new ResultadoPrevisao(
                Math.round(mediaIndiceSeguinte * 10) / 10.0,
                Math.round(perigosos * 1000.0 / analogos.size()) / 1000.0,
                analogos.size());
    }

    /** Pares (dia, dia seguinte) consecutivos que terminam até o dia base: nada do futuro entra no cálculo. */
    static List<ParDias> montarPares(List<ObservacaoClimatica> historico, LocalDate dataBase) {
        List<ParDias> pares = new ArrayList<>();
        for (int i = 0; i + 1 < historico.size(); i++) {
            ObservacaoClimatica dia = historico.get(i);
            ObservacaoClimatica seguinte = historico.get(i + 1);
            boolean consecutivos = seguinte.getData().equals(dia.getData().plusDays(1));
            if (consecutivos && !seguinte.getData().isAfter(dataBase)) {
                pares.add(new ParDias(dia, seguinte));
            }
        }
        return pares;
    }

    private static List<ParDias> filtrar(List<ParDias> pares, ObservacaoClimatica base, double tolerancia, boolean mesmaEpoca) {
        return pares.stream()
                .filter(p -> Math.abs(p.dia().getIndiceCalorMaximo() - base.getIndiceCalorMaximo()) <= tolerancia)
                .filter(p -> !mesmaEpoca || distanciaMeses(p.dia().getData(), base.getData()) <= DISTANCIA_MESES)
                .toList();
    }

    /** Distância circular entre meses (dezembro e janeiro estão a 1 mês de distância). */
    static int distanciaMeses(LocalDate a, LocalDate b) {
        int diferenca = Math.abs(a.getMonthValue() - b.getMonthValue());
        return Math.min(diferenca, 12 - diferenca);
    }

    record ParDias(ObservacaoClimatica dia, ObservacaoClimatica seguinte) {
    }
}
