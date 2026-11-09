package br.com.fiap3espg.heatsafe.service;

import br.com.fiap3espg.heatsafe.domain.ValidacaoException;
import br.com.fiap3espg.heatsafe.domain.estacao.Estacao;
import br.com.fiap3espg.heatsafe.domain.observacao.*;
import br.com.fiap3espg.heatsafe.domain.risco.NivelRisco;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ObservacaoService {
    private static final LocalDate INICIO_PADRAO = LocalDate.of(1900, 1, 1);

    private final ObservacaoRepository repository;
    private final EstacaoService estacaoService;

    @Transactional
    public DadosDetalhamentoObservacao cadastrarObservacao(String codigoEstacao, DadosCadastroObservacao dados) {
        Estacao estacao = estacaoService.buscarAtiva(codigoEstacao);
        if (repository.existsByEstacaoAndData(estacao, dados.data())) {
            throw new ObservacaoDuplicadaException(estacao.getCodigo(), dados.data());
        }
        ObservacaoClimatica salva = repository.save(new ObservacaoClimatica(estacao, dados));
        return new DadosDetalhamentoObservacao(salva);
    }

    @Transactional(readOnly = true)
    public Page<DadosDetalhamentoObservacao> listarObservacoes(String codigoEstacao, LocalDate inicio, LocalDate fim,
                                                               boolean somentePerigosos, Pageable paginacao) {
        Estacao estacao = estacaoService.buscarAtiva(codigoEstacao);
        LocalDate de = inicio != null ? inicio : INICIO_PADRAO;
        LocalDate ate = fim != null ? fim : LocalDate.now();
        if (de.isAfter(ate)) {
            throw new ValidacaoException("A data inicial não pode ser depois da data final");
        }
        Page<ObservacaoClimatica> pagina = somentePerigosos
                ? repository.findByEstacaoAndDataBetweenAndIndiceCalorMaximoGreaterThanEqual(
                        estacao, de, ate, NivelRisco.LIMIAR_DIA_PERIGOSO, paginacao)
                : repository.findByEstacaoAndDataBetween(estacao, de, ate, paginacao);
        return pagina.map(DadosDetalhamentoObservacao::new);
    }

    @Transactional(readOnly = true)
    public DadosDetalhamentoObservacao detalharObservacao(String codigoEstacao, LocalDate data) {
        return new DadosDetalhamentoObservacao(buscar(codigoEstacao, data));
    }

    @Transactional
    public void excluirObservacao(String codigoEstacao, LocalDate data) {
        repository.delete(buscar(codigoEstacao, data));
    }

    private ObservacaoClimatica buscar(String codigoEstacao, LocalDate data) {
        Estacao estacao = estacaoService.buscarAtiva(codigoEstacao);
        return repository.findByEstacaoAndData(estacao, data)
                .orElseThrow(() -> new ObservacaoNotFoundException(
                        "Não há observação da estação " + estacao.getCodigo() + " no dia " + data));
    }
}
