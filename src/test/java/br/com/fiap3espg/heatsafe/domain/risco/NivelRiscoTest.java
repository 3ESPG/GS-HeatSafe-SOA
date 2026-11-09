package br.com.fiap3espg.heatsafe.domain.risco;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NivelRiscoTest {

    @Test
    @DisplayName("Classifica o índice de calor nas faixas da NOAA (limite inferior incluído)")
    void faixas() {
        assertThat(NivelRisco.de(26.9)).isEqualTo(NivelRisco.NORMAL);
        assertThat(NivelRisco.de(27.0)).isEqualTo(NivelRisco.ATENCAO);
        assertThat(NivelRisco.de(32.0)).isEqualTo(NivelRisco.CAUTELA_EXTREMA);
        assertThat(NivelRisco.de(41.0)).isEqualTo(NivelRisco.PERIGO);
        assertThat(NivelRisco.de(54.0)).isEqualTo(NivelRisco.PERIGO_EXTREMO);
    }

    @Test
    @DisplayName("Considera perigoso a partir de Cautela extrema")
    void perigoso() {
        assertThat(NivelRisco.ATENCAO.isPerigoso()).isFalse();
        assertThat(NivelRisco.CAUTELA_EXTREMA.isPerigoso()).isTrue();
        assertThat(NivelRisco.PERIGO_EXTREMO.isPerigoso()).isTrue();
    }
}
