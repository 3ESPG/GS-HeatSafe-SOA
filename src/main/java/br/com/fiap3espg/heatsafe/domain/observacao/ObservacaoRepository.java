package br.com.fiap3espg.heatsafe.domain.observacao;

import br.com.fiap3espg.heatsafe.domain.estacao.Estacao;
import br.com.fiap3espg.heatsafe.domain.estatistica.DadosResumoAnual;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ObservacaoRepository extends JpaRepository<ObservacaoClimatica, Long> {
    long countByEstacao(Estacao estacao);

    boolean existsByEstacaoAndData(Estacao estacao, LocalDate data);

    Optional<ObservacaoClimatica> findByEstacaoAndData(Estacao estacao, LocalDate data);

    Optional<ObservacaoClimatica> findFirstByEstacaoOrderByDataDesc(Estacao estacao);

    Page<ObservacaoClimatica> findByEstacaoAndDataBetween(Estacao estacao, LocalDate inicio, LocalDate fim, Pageable paginacao);

    Page<ObservacaoClimatica> findByEstacaoAndDataBetweenAndIndiceCalorMaximoGreaterThanEqual(
            Estacao estacao, LocalDate inicio, LocalDate fim, Double indiceMinimo, Pageable paginacao);

    /** Histórico completo até uma data, em ordem cronológica (usado pelo modelo de previsão). */
    List<ObservacaoClimatica> findByEstacaoAndDataLessThanEqualOrderByDataAsc(Estacao estacao, LocalDate data);

    List<ObservacaoClimatica> findTop10ByEstacaoOrderByIndiceCalorMaximoDesc(Estacao estacao);

    @Query("select o.data from ObservacaoClimatica o where o.estacao = :estacao")
    List<LocalDate> listarDatasCadastradas(@Param("estacao") Estacao estacao);

    @Query("""
            select new br.com.fiap3espg.heatsafe.domain.estatistica.DadosResumoAnual(
                year(o.data),
                count(o),
                sum(case when o.indiceCalorMaximo >= :limiar then 1L else 0L end),
                max(o.indiceCalorMaximo),
                avg(o.indiceCalorMaximo),
                max(o.temperaturaMaxima))
            from ObservacaoClimatica o
            where o.estacao = :estacao
            group by year(o.data)
            order by year(o.data)
            """)
    List<DadosResumoAnual> resumoAnual(@Param("estacao") Estacao estacao, @Param("limiar") double limiar);
}
