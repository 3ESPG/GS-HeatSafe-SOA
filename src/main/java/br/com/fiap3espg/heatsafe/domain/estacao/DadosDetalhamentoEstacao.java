package br.com.fiap3espg.heatsafe.domain.estacao;

public record DadosDetalhamentoEstacao(
        Long id,
        String codigo,
        String nome,
        String cidade,
        String uf,
        Double latitude,
        Double longitude,
        Double altitude,
        boolean ativa,
        long totalObservacoes) {

    public DadosDetalhamentoEstacao(Estacao estacao, long totalObservacoes) {
        this(estacao.getId(), estacao.getCodigo(), estacao.getNome(), estacao.getCidade(), estacao.getUf(),
                estacao.getLatitude(), estacao.getLongitude(), estacao.getAltitude(), estacao.isAtiva(), totalObservacoes);
    }
}
