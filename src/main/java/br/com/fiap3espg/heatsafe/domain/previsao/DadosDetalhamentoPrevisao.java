package br.com.fiap3espg.heatsafe.domain.previsao;

import br.com.fiap3espg.heatsafe.domain.risco.NivelRisco;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DadosDetalhamentoPrevisao(
        Long id,
        String estacao,
        LocalDate dataBase,
        LocalDate dataPrevista,
        Double indiceCalorPrevisto,
        Double probabilidadePerigo,
        boolean perigosa,
        NivelRisco nivelRisco,
        String orientacao,
        MetodoPrevisao metodo,
        Integer amostrasHistoricas,
        LocalDateTime criadaEm) {

    public DadosDetalhamentoPrevisao(Previsao p) {
        this(p.getId(), p.getEstacao().getCodigo(), p.getDataBase(), p.getDataPrevista(), p.getIndiceCalorPrevisto(),
                p.getProbabilidadePerigo(), p.isPerigosa(), p.getNivelRisco(), p.getNivelRisco().getOrientacao(),
                p.getMetodo(), p.getAmostrasHistoricas(), p.getCriadaEm());
    }
}
