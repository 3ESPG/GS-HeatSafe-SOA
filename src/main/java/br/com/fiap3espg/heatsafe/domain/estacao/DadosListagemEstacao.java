package br.com.fiap3espg.heatsafe.domain.estacao;

public record DadosListagemEstacao(String codigo, String nome, String cidade, String uf) {

    public DadosListagemEstacao(Estacao estacao) {
        this(estacao.getCodigo(), estacao.getNome(), estacao.getCidade(), estacao.getUf());
    }
}
