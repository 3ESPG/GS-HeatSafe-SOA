package br.com.fiap3espg.heatsafe.service;

import br.com.fiap3espg.heatsafe.domain.estacao.Estacao;
import br.com.fiap3espg.heatsafe.domain.estatistica.DadosResumoAnual;
import br.com.fiap3espg.heatsafe.domain.observacao.DadosDetalhamentoObservacao;
import br.com.fiap3espg.heatsafe.domain.observacao.ObservacaoRepository;
import br.com.fiap3espg.heatsafe.domain.risco.NivelRisco;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EstatisticaService {
    private final ObservacaoRepository observacaoRepository;
    private final EstacaoService estacaoService;

    @Transactional(readOnly = true)
    public List<DadosResumoAnual> resumoAnual(String codigoEstacao) {
        Estacao estacao = estacaoService.buscarAtiva(codigoEstacao);
        return observacaoRepository.resumoAnual(estacao, NivelRisco.LIMIAR_DIA_PERIGOSO);
    }

    @Transactional(readOnly = true)
    public List<DadosDetalhamentoObservacao> diasMaisQuentes(String codigoEstacao) {
        Estacao estacao = estacaoService.buscarAtiva(codigoEstacao);
        return observacaoRepository.findTop10ByEstacaoOrderByIndiceCalorMaximoDesc(estacao).stream()
                .map(DadosDetalhamentoObservacao::new)
                .toList();
    }
}
