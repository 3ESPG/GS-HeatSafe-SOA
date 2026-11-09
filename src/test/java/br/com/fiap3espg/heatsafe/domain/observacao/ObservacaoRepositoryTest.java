package br.com.fiap3espg.heatsafe.domain.observacao;

import br.com.fiap3espg.heatsafe.domain.estacao.Estacao;
import br.com.fiap3espg.heatsafe.domain.estacao.EstacaoRepository;
import br.com.fiap3espg.heatsafe.domain.estatistica.DadosResumoAnual;
import br.com.fiap3espg.heatsafe.domain.risco.NivelRisco;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
// Usa o H2 em modo MySQL do perfil de teste (em vez do H2 padrão) para rodar as migrações do Flyway
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class ObservacaoRepositoryTest {
    @Autowired
    ObservacaoRepository repository;

    @Autowired
    EstacaoRepository estacaoRepository;

    @Autowired
    TestEntityManager testEntity;

    Estacao estacao;

    @BeforeEach
    void setUp() {
        // A estação A701 é inserida pela migração V4 do Flyway
        estacao = estacaoRepository.findByCodigoAndAtivaTrue("A701").orElseThrow();
    }

    private ObservacaoClimatica persistir(LocalDate data, double indiceCalor) {
        ObservacaoClimatica o = ObservacaoClimatica.importada(estacao, data, 30, 20, 25.0, 60.0, 40, 0.0, indiceCalor);
        return testEntity.persistAndFlush(o);
    }

    @Test
    @DisplayName("Migrações do Flyway criam o esquema e cadastram a estação A701")
    void migracoes() {
        assertThat(estacao.getNome()).isEqualTo("SAO PAULO - MIRANTE");
        assertThat(estacao.getUf()).isEqualTo("SP");
    }

    @Test
    @DisplayName("Não permite duas observações da mesma estação no mesmo dia")
    void unicidadeEstacaoData() {
        persistir(LocalDate.of(2024, 1, 1), 30);
        assertThatThrownBy(() -> repository.saveAndFlush(
                ObservacaoClimatica.importada(estacao, LocalDate.of(2024, 1, 1), 31, 21, null, null, 45, null, 31)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Resumo anual conta os dias perigosos (índice ≥ 32 °C) de cada ano")
    void resumoAnual() {
        persistir(LocalDate.of(2023, 11, 14), 33.0);
        persistir(LocalDate.of(2023, 11, 15), 35.5);
        persistir(LocalDate.of(2023, 11, 16), 28.0);
        persistir(LocalDate.of(2024, 2, 1), 31.9);

        List<DadosResumoAnual> resumo = repository.resumoAnual(estacao, NivelRisco.LIMIAR_DIA_PERIGOSO);

        assertThat(resumo).hasSize(2);
        assertThat(resumo.get(0).ano()).isEqualTo(2023);
        assertThat(resumo.get(0).diasComDados()).isEqualTo(3);
        assertThat(resumo.get(0).diasPerigosos()).isEqualTo(2);
        assertThat(resumo.get(0).percentualDiasPerigosos()).isEqualTo(66.7);
        assertThat(resumo.get(0).maiorIndiceCalor()).isEqualTo(35.5);
        assertThat(resumo.get(1).diasPerigosos()).isZero();
    }

    @Test
    @DisplayName("Histórico até uma data vem em ordem cronológica e sem dias posteriores")
    void historicoOrdenado() {
        persistir(LocalDate.of(2024, 1, 3), 30);
        persistir(LocalDate.of(2024, 1, 1), 30);
        persistir(LocalDate.of(2024, 1, 2), 30);
        persistir(LocalDate.of(2024, 1, 4), 30);

        List<ObservacaoClimatica> historico =
                repository.findByEstacaoAndDataLessThanEqualOrderByDataAsc(estacao, LocalDate.of(2024, 1, 3));

        assertThat(historico).extracting(ObservacaoClimatica::getData)
                .containsExactly(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 2), LocalDate.of(2024, 1, 3));
    }
}
