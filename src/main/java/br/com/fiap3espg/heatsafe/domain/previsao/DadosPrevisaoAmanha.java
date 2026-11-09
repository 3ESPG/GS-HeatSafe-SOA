package br.com.fiap3espg.heatsafe.domain.previsao;

import br.com.fiap3espg.heatsafe.domain.risco.NivelRisco;

import java.time.LocalDate;

/** Resposta enxuta consumida pela API C# do HeatSafe para ligar o "modo vigilância". */
public record DadosPrevisaoAmanha(
        String estacao,
        LocalDate data,
        boolean perigoso,
        Double probabilidade,
        Double indiceCalorPrevisto,
        NivelRisco nivelRisco) {

    public DadosPrevisaoAmanha(Previsao p) {
        this(p.getEstacao().getCodigo(), p.getDataPrevista(), p.isPerigosa(), p.getProbabilidadePerigo(),
                p.getIndiceCalorPrevisto(), p.getNivelRisco());
    }
}
