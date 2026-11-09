package br.com.fiap3espg.heatsafe.domain.observacao;

import br.com.fiap3espg.heatsafe.domain.estacao.Estacao;
import br.com.fiap3espg.heatsafe.domain.risco.CalculadoraIndiceCalor;
import br.com.fiap3espg.heatsafe.domain.risco.NivelRisco;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/** Resumo diário do clima medido por uma estação. É a base do histórico usado na previsão. */
@Entity(name = "ObservacaoClimatica")
@Table(name = "observacoes_climaticas")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode(of = "id")
public class ObservacaoClimatica {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estacao_id")
    private Estacao estacao;

    private LocalDate data;
    private Double temperaturaMaxima;
    private Double temperaturaMinima;
    private Double temperaturaMedia;
    private Double umidadeMedia;
    private Double umidadeMinima;
    private Double precipitacao;
    private Double indiceCalorMaximo;

    @Enumerated(EnumType.STRING)
    private NivelRisco nivelRisco;

    @Enumerated(EnumType.STRING)
    private OrigemObservacao origem;

    /**
     * Registro manual: o índice de calor máximo é estimado com a temperatura máxima e a umidade mínima,
     * que normalmente acontecem juntas, no horário mais quente do dia.
     */
    public ObservacaoClimatica(Estacao estacao, DadosCadastroObservacao dados) {
        this(null, estacao, dados.data(), dados.temperaturaMaxima(), dados.temperaturaMinima(), dados.temperaturaMedia(),
                dados.umidadeMedia(), dados.umidadeMinima(), dados.precipitacao(),
                CalculadoraIndiceCalor.calcular(dados.temperaturaMaxima(), dados.umidadeMinima()),
                null, OrigemObservacao.MANUAL);
        this.nivelRisco = NivelRisco.de(this.indiceCalorMaximo);
    }

    /** Importação do INMET: o índice de calor máximo já vem calculado hora a hora. */
    public static ObservacaoClimatica importada(Estacao estacao, LocalDate data, double temperaturaMaxima,
                                                double temperaturaMinima, Double temperaturaMedia, Double umidadeMedia,
                                                double umidadeMinima, Double precipitacao, double indiceCalorMaximo) {
        return new ObservacaoClimatica(null, estacao, data, temperaturaMaxima, temperaturaMinima, temperaturaMedia,
                umidadeMedia, umidadeMinima, precipitacao, indiceCalorMaximo, NivelRisco.de(indiceCalorMaximo),
                OrigemObservacao.INMET);
    }

    public boolean isDiaPerigoso() {
        return indiceCalorMaximo >= NivelRisco.LIMIAR_DIA_PERIGOSO;
    }
}
