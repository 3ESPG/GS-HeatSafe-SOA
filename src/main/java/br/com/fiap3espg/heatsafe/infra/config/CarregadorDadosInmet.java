package br.com.fiap3espg.heatsafe.infra.config;

import br.com.fiap3espg.heatsafe.domain.estacao.Estacao;
import br.com.fiap3espg.heatsafe.domain.observacao.DadosResultadoImportacao;
import br.com.fiap3espg.heatsafe.domain.observacao.ObservacaoRepository;
import br.com.fiap3espg.heatsafe.service.EstacaoService;
import br.com.fiap3espg.heatsafe.service.ImportacaoObservacoesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * Na primeira execução, carrega no banco as observações reais do INMET que acompanham o projeto,
 * para a API já subir com 11 anos de histórico. Se a estação já tiver dados, não faz nada.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.dados.carregar-inmet", havingValue = "true")
public class CarregadorDadosInmet implements ApplicationRunner {
    private final EstacaoService estacaoService;
    private final ObservacaoRepository observacaoRepository;
    private final ImportacaoObservacoesService importacaoService;

    @Value("${app.dados.arquivo-inmet}")
    private Resource arquivo;

    @Value("${app.dados.estacao-inmet}")
    private String codigoEstacao;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Estacao estacao = estacaoService.buscarAtiva(codigoEstacao);
        if (observacaoRepository.countByEstacao(estacao) > 0) {
            log.info("Estação {} já tem observações: carga inicial do INMET ignorada.", codigoEstacao);
            return;
        }
        log.info("Carregando observações reais do INMET da estação {} ({})...", codigoEstacao, arquivo.getFilename());
        try (InputStream csv = arquivo.getInputStream()) {
            DadosResultadoImportacao resultado = importacaoService.importar(codigoEstacao, csv);
            log.info("Carga concluída: {} observações importadas ({} a {}), {} linhas incompletas ignoradas.",
                    resultado.importadas(), resultado.primeiraData(), resultado.ultimaData(),
                    resultado.ignoradasIncompletas());
        }
    }
}
