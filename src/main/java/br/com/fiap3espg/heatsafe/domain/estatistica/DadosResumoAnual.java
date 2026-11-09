package br.com.fiap3espg.heatsafe.domain.estatistica;

/** Resumo de um ano de observações: quantos dias foram perigosos e qual foi o pior calor. */
public record DadosResumoAnual(
        Integer ano,
        Long diasComDados,
        Long diasPerigosos,
        Double percentualDiasPerigosos,
        Double maiorIndiceCalor,
        Double mediaIndiceCalorMaximo,
        Double maiorTemperatura) {

    /** Construtor usado pela consulta JPQL agregada (ObservacaoRepository.resumoAnual). */
    public DadosResumoAnual(Integer ano, Long diasComDados, Long diasPerigosos, Double maiorIndiceCalor,
                            Double mediaIndiceCalorMaximo, Double maiorTemperatura) {
        this(ano, diasComDados, diasPerigosos,
                diasComDados == 0 ? 0.0 : Math.round(diasPerigosos * 1000.0 / diasComDados) / 10.0,
                maiorIndiceCalor,
                mediaIndiceCalorMaximo == null ? null : Math.round(mediaIndiceCalorMaximo * 10) / 10.0,
                maiorTemperatura);
    }
}
