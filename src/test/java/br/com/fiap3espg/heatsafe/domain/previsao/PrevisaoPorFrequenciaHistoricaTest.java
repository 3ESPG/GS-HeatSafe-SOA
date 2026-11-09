package br.com.fiap3espg.heatsafe.domain.previsao;

import br.com.fiap3espg.heatsafe.domain.ValidacaoException;
import br.com.fiap3espg.heatsafe.domain.estacao.Estacao;
import br.com.fiap3espg.heatsafe.domain.observacao.ObservacaoClimatica;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PrevisaoPorFrequenciaHistoricaTest {
    private final PrevisaoPorFrequenciaHistorica modelo = new PrevisaoPorFrequenciaHistorica();
    private final Estacao estacao = new Estacao(1L, "A701", "Teste", "São Paulo", "SP", -23.5, -46.6, 792.0, true);

    private ObservacaoClimatica dia(LocalDate data, double indiceCalor) {
        return ObservacaoClimatica.importada(estacao, data, 30, 20, 25.0, 60.0, 40, 0.0, indiceCalor);
    }

    @Test
    @DisplayName("Probabilidade é a fração de dias análogos cujo dia seguinte foi perigoso")
    void probabilidadePorDiasAnalogos() {
        // Janeiro: dias de 31 °C seguidos alternadamente de 33 °C (perigoso) e 29 °C
        List<ObservacaoClimatica> historico = new ArrayList<>();
        LocalDate data = LocalDate.of(2020, 1, 1);
        for (int i = 0; i < 30; i++) {
            historico.add(dia(data, 31.0));
            historico.add(dia(data.plusDays(1), i % 2 == 0 ? 33.0 : 29.0));
            data = data.plusDays(2);
            if (data.getMonthValue() > 2) {
                data = data.withYear(data.getYear() + 1).withMonth(1).withDayOfMonth(1);
            }
        }
        ObservacaoClimatica base = dia(LocalDate.of(2025, 1, 10), 31.0);
        historico.add(base);

        ResultadoPrevisao resultado = modelo.prever(base, historico);

        assertThat(resultado.probabilidadePerigo()).isEqualTo(0.5);
        assertThat(resultado.indiceCalorPrevisto()).isEqualTo(31.0);
        assertThat(resultado.amostrasHistoricas()).isEqualTo(30);
    }

    @Test
    @DisplayName("Não usa dias depois do dia base (sem vazamento de dados do futuro)")
    void semDadosDoFuturo() {
        LocalDate base = LocalDate.of(2024, 1, 10);
        List<ObservacaoClimatica> historico = List.of(dia(base, 31), dia(base.plusDays(1), 35), dia(base.plusDays(2), 31));
        assertThat(PrevisaoPorFrequenciaHistorica.montarPares(historico, base)).isEmpty();
    }

    @Test
    @DisplayName("Ignora pares de dias não consecutivos (buracos nos dados)")
    void ignoraBuracos() {
        LocalDate d = LocalDate.of(2024, 1, 1);
        List<ObservacaoClimatica> historico = List.of(dia(d, 30), dia(d.plusDays(1), 30), dia(d.plusDays(5), 30));
        assertThat(PrevisaoPorFrequenciaHistorica.montarPares(historico, d.plusDays(10))).hasSize(1);
    }

    @Test
    @DisplayName("Lança erro quando o histórico não tem dias parecidos suficientes")
    void historicoInsuficiente() {
        ObservacaoClimatica base = dia(LocalDate.of(2024, 1, 3), 31);
        List<ObservacaoClimatica> historico = List.of(dia(LocalDate.of(2024, 1, 1), 31), dia(LocalDate.of(2024, 1, 2), 33), base);
        assertThatThrownBy(() -> modelo.prever(base, historico)).isInstanceOf(ValidacaoException.class);
    }

    @Test
    @DisplayName("Distância entre meses é circular (dezembro e janeiro são vizinhos)")
    void distanciaCircular() {
        assertThat(PrevisaoPorFrequenciaHistorica.distanciaMeses(LocalDate.of(2024, 12, 1), LocalDate.of(2025, 1, 1))).isEqualTo(1);
        assertThat(PrevisaoPorFrequenciaHistorica.distanciaMeses(LocalDate.of(2024, 6, 1), LocalDate.of(2025, 1, 1))).isEqualTo(5);
    }
}
