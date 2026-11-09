package br.com.fiap3espg.heatsafe.service;

import br.com.fiap3espg.heatsafe.domain.estacao.*;
import br.com.fiap3espg.heatsafe.domain.observacao.ObservacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EstacaoService {
    private final EstacaoRepository repository;
    private final ObservacaoRepository observacaoRepository;

    @Transactional
    public DadosDetalhamentoEstacao cadastrarEstacao(DadosCadastroEstacao dados) {
        String codigo = dados.codigo().toUpperCase();
        if (repository.existsByCodigo(codigo)) {
            throw new EstacaoJaCadastradaException(codigo);
        }
        Estacao salva = repository.save(new Estacao(dados));
        return new DadosDetalhamentoEstacao(salva, 0);
    }

    @Transactional(readOnly = true)
    public Page<DadosListagemEstacao> listarEstacoes(Pageable paginacao) {
        return repository.findAllByAtivaTrue(paginacao).map(DadosListagemEstacao::new);
    }

    @Transactional(readOnly = true)
    public DadosDetalhamentoEstacao detalharEstacao(String codigo) {
        Estacao estacao = buscarAtiva(codigo);
        return new DadosDetalhamentoEstacao(estacao, observacaoRepository.countByEstacao(estacao));
    }

    @Transactional
    public DadosDetalhamentoEstacao atualizarEstacao(String codigo, DadosAtualizacaoEstacao dados) {
        Estacao estacao = buscarAtiva(codigo);
        estacao.atualizarInformacoes(dados);
        return new DadosDetalhamentoEstacao(repository.save(estacao), observacaoRepository.countByEstacao(estacao));
    }

    @Transactional
    public void desativarEstacao(String codigo) {
        Estacao estacao = buscarAtiva(codigo);
        estacao.desativar();
        repository.save(estacao);
    }

    /** Usado pelos outros serviços: só estações ativas recebem dados e previsões. */
    @Transactional(readOnly = true)
    public Estacao buscarAtiva(String codigo) {
        return repository.findByCodigoAndAtivaTrue(codigo.toUpperCase())
                .orElseThrow(() -> new EstacaoNotFoundException(codigo.toUpperCase()));
    }
}
