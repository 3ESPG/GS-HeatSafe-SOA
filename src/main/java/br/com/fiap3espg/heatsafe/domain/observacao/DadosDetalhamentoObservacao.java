package br.com.fiap3espg.heatsafe.domain.observacao;

import br.com.fiap3espg.heatsafe.domain.risco.NivelRisco;

import java.time.LocalDate;

public record DadosDetalhamentoObservacao(
        Long id,
        String estacao,
        LocalDate data,
        Double temperaturaMaxima,
        Double temperaturaMinima,
        Double temperaturaMedia,
        Double umidadeMedia,
        Double umidadeMinima,
        Double precipitacao,
        Double indiceCalorMaximo,
        NivelRisco nivelRisco,
        boolean diaPerigoso,
        OrigemObservacao origem) {

    public DadosDetalhamentoObservacao(ObservacaoClimatica o) {
        this(o.getId(), o.getEstacao().getCodigo(), o.getData(), o.getTemperaturaMaxima(), o.getTemperaturaMinima(),
                o.getTemperaturaMedia(), o.getUmidadeMedia(), o.getUmidadeMinima(), o.getPrecipitacao(),
                o.getIndiceCalorMaximo(), o.getNivelRisco(), o.isDiaPerigoso(), o.getOrigem());
    }
}
