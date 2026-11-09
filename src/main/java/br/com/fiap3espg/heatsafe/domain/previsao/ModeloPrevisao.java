package br.com.fiap3espg.heatsafe.domain.previsao;

import br.com.fiap3espg.heatsafe.domain.observacao.ObservacaoClimatica;

import java.util.List;

/**
 * Contrato dos modelos de previsão (padrão Strategy).
 * Permite trocar o modelo estatístico pelo modelo de ML sem alterar o serviço nem a API.
 */
public interface ModeloPrevisao {

    MetodoPrevisao metodo();

    /**
     * @param diaBase   último dia observado (a previsão é para o dia seguinte)
     * @param historico observações da estação até o dia base, em ordem cronológica
     */
    ResultadoPrevisao prever(ObservacaoClimatica diaBase, List<ObservacaoClimatica> historico);
}
