package br.com.fiap3espg.heatsafe.domain.previsao;

import br.com.fiap3espg.heatsafe.domain.estacao.Estacao;
import br.com.fiap3espg.heatsafe.domain.risco.NivelRisco;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Previsão gerada para o dia seguinte a um dia observado. Fica salva para consulta e auditoria. */
@Entity(name = "Previsao")
@Table(name = "previsoes")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode(of = "id")
public class Previsao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estacao_id")
    private Estacao estacao;

    private LocalDate dataBase;
    private LocalDate dataPrevista;
    private Double indiceCalorPrevisto;
    private Double probabilidadePerigo;
    private boolean perigosa;

    @Enumerated(EnumType.STRING)
    private NivelRisco nivelRisco;

    @Enumerated(EnumType.STRING)
    private MetodoPrevisao metodo;

    private Integer amostrasHistoricas;
    private LocalDateTime criadaEm;

    public Previsao(Estacao estacao, LocalDate dataBase, ResultadoPrevisao resultado, MetodoPrevisao metodo,
                    double limiarProbabilidade) {
        this.estacao = estacao;
        this.dataBase = dataBase;
        this.dataPrevista = dataBase.plusDays(1);
        this.indiceCalorPrevisto = resultado.indiceCalorPrevisto();
        this.probabilidadePerigo = resultado.probabilidadePerigo();
        this.perigosa = resultado.probabilidadePerigo() >= limiarProbabilidade;
        this.nivelRisco = NivelRisco.de(resultado.indiceCalorPrevisto());
        this.metodo = metodo;
        this.amostrasHistoricas = resultado.amostrasHistoricas();
        this.criadaEm = LocalDateTime.now();
    }
}
