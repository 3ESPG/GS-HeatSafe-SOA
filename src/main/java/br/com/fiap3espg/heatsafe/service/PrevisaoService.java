package br.com.fiap3espg.heatsafe.service;

import br.com.fiap3espg.heatsafe.domain.ValidacaoException;
import br.com.fiap3espg.heatsafe.domain.estacao.Estacao;
import br.com.fiap3espg.heatsafe.domain.observacao.ObservacaoClimatica;
import br.com.fiap3espg.heatsafe.domain.observacao.ObservacaoNotFoundException;
import br.com.fiap3espg.heatsafe.domain.observacao.ObservacaoRepository;
import br.com.fiap3espg.heatsafe.domain.previsao.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class PrevisaoService {
    private final PrevisaoRepository repository;
    private final ObservacaoRepository observacaoRepository;
    private final EstacaoService estacaoService;
    private final ModeloPrevisao modelo;
    private final double limiarProbabilidade;

    public PrevisaoService(PrevisaoRepository repository, ObservacaoRepository observacaoRepository,
                           EstacaoService estacaoService, ModeloPrevisao modelo,
                           @Value("${app.previsao.limiar-probabilidade:0.2}") double limiarProbabilidade) {
        this.repository = repository;
        this.observacaoRepository = observacaoRepository;
        this.estacaoService = estacaoService;
        this.modelo = modelo;
        this.limiarProbabilidade = limiarProbabilidade;
    }

    /** Gera e salva a previsão para o dia seguinte ao dia base (ou ao último dia com dados). */
    @Transactional
    public DadosDetalhamentoPrevisao gerarPrevisao(String codigoEstacao, LocalDate dataBase) {
        return new DadosDetalhamentoPrevisao(gerar(codigoEstacao, dataBase));
    }

    /** Contrato usado pela API C#: previsão para o dia seguinte ao último dia observado. */
    @Transactional
    public DadosPrevisaoAmanha preverAmanha(String codigoEstacao) {
        return new DadosPrevisaoAmanha(gerar(codigoEstacao, null));
    }

    @Transactional(readOnly = true)
    public Page<DadosDetalhamentoPrevisao> listarPrevisoes(String codigoEstacao, Pageable paginacao) {
        Estacao estacao = estacaoService.buscarAtiva(codigoEstacao);
        return repository.findByEstacao(estacao, paginacao).map(DadosDetalhamentoPrevisao::new);
    }

    @Transactional(readOnly = true)
    public DadosDetalhamentoPrevisao detalharPrevisao(Long id) {
        return repository.findById(id)
                .map(DadosDetalhamentoPrevisao::new)
                .orElseThrow(() -> new PrevisaoNotFoundException(id));
    }

    /**
     * Backtest: para cada dia do período, prevê o dia seguinte usando só o histórico até aquele dia
     * e compara com o que aconteceu de verdade.
     */
    @Transactional(readOnly = true)
    public DadosAvaliacaoModelo avaliarModelo(String codigoEstacao, LocalDate inicio, LocalDate fim, Double limiar) {
        double limiarUsado = limiar != null ? limiar : limiarProbabilidade;
        if (limiarUsado <= 0 || limiarUsado >= 1) {
            throw new ValidacaoException("O limiar de probabilidade deve estar entre 0 e 1");
        }
        if (inicio.isAfter(fim)) {
            throw new ValidacaoException("A data inicial não pode ser depois da data final");
        }
        Estacao estacao = estacaoService.buscarAtiva(codigoEstacao);
        List<ObservacaoClimatica> historico =
                observacaoRepository.findByEstacaoAndDataLessThanEqualOrderByDataAsc(estacao, fim.plusDays(1));

        int vp = 0, fp = 0, fn = 0, vn = 0;
        for (int i = 0; i + 1 < historico.size(); i++) {
            ObservacaoClimatica dia = historico.get(i);
            ObservacaoClimatica seguinte = historico.get(i + 1);
            boolean noPeriodo = !dia.getData().isBefore(inicio) && !dia.getData().isAfter(fim);
            if (!noPeriodo || !seguinte.getData().equals(dia.getData().plusDays(1))) {
                continue;
            }
            ResultadoPrevisao resultado;
            try {
                resultado = modelo.prever(dia, historico.subList(0, i + 1));
            } catch (ValidacaoException historicoInsuficiente) {
                continue;
            }
            boolean previuPerigo = resultado.probabilidadePerigo() >= limiarUsado;
            boolean foiPerigoso = seguinte.isDiaPerigoso();
            if (previuPerigo && foiPerigoso) vp++;
            else if (previuPerigo) fp++;
            else if (foiPerigoso) fn++;
            else vn++;
        }

        int total = vp + fp + fn + vn;
        if (total == 0) {
            throw new ValidacaoException("Não há dias suficientes com dados no período para avaliar o modelo");
        }
        double recall = razao(vp, vp + fn);
        double precisao = razao(vp, vp + fp);
        double f1 = recall + precisao == 0 ? 0 : arredondar(2 * recall * precisao / (recall + precisao));
        return new DadosAvaliacaoModelo(estacao.getCodigo(), modelo.metodo(), inicio, fim, limiarUsado, total,
                vp, fp, fn, vn, recall, precisao, f1, razao(vp + vn, total));
    }

    private Previsao gerar(String codigoEstacao, LocalDate dataBase) {
        Estacao estacao = estacaoService.buscarAtiva(codigoEstacao);
        ObservacaoClimatica diaBase = (dataBase == null
                ? observacaoRepository.findFirstByEstacaoOrderByDataDesc(estacao)
                : observacaoRepository.findByEstacaoAndData(estacao, dataBase))
                .orElseThrow(() -> new ObservacaoNotFoundException(dataBase == null
                        ? "A estação " + estacao.getCodigo() + " ainda não tem observações"
                        : "Não há observação da estação " + estacao.getCodigo() + " no dia " + dataBase));

        List<ObservacaoClimatica> historico =
                observacaoRepository.findByEstacaoAndDataLessThanEqualOrderByDataAsc(estacao, diaBase.getData());
        ResultadoPrevisao resultado = modelo.prever(diaBase, historico);
        return repository.save(new Previsao(estacao, diaBase.getData(), resultado, modelo.metodo(), limiarProbabilidade));
    }

    private static double razao(int parte, int todo) {
        return todo == 0 ? 0 : arredondar((double) parte / todo);
    }

    private static double arredondar(double valor) {
        return Math.round(valor * 1000) / 1000.0;
    }
}
