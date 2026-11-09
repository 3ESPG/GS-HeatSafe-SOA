package br.com.fiap3espg.heatsafe.domain.risco;

import br.com.fiap3espg.heatsafe.domain.ValidacaoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CalculadoraIndiceCalorTest {

    @Test
    @DisplayName("Usa a fórmula simples da NOAA em temperaturas amenas")
    void temperaturaAmena() {
        assertThat(CalculadoraIndiceCalor.calcular(24.5, 55)).isEqualTo(24.4);
    }

    @Test
    @DisplayName("Usa a regressão de Rothfusz no calor (mesmos valores do script Python do INMET)")
    void calorForte() {
        assertThat(CalculadoraIndiceCalor.calcular(34, 50)).isEqualTo(38.4);
        assertThat(CalculadoraIndiceCalor.calcular(38, 52)).isEqualTo(49.8);
    }

    @Test
    @DisplayName("Rejeita umidade fora de 0 a 100%")
    void umidadeInvalida() {
        assertThatThrownBy(() -> CalculadoraIndiceCalor.calcular(30, 150)).isInstanceOf(ValidacaoException.class);
        assertThatThrownBy(() -> CalculadoraIndiceCalor.calcular(30, -1)).isInstanceOf(ValidacaoException.class);
    }
}
