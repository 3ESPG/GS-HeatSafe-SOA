package br.com.fiap3espg.heatsafe.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Fluxo completo: importa um CSV no formato do INMET, consulta, registra e gera previsões. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ObservacaoPrevisaoIntegracaoTest {
    @Autowired
    MockMvc mvc;

    /** 60 dias de janeiro/fevereiro: dias de 31 °C seguidos de dias de 33 °C (perigosos) ou 29 °C. */
    private MockMultipartFile csvSintetico() {
        StringBuilder csv = new StringBuilder("data,horas_validas,temp_max_c,temp_min_c,temp_media_c,umidade_media_pct,"
                + "umidade_min_pct,precipitacao_mm,pressao_media_mb,radiacao_total_kj_m2,vento_medio_ms,"
                + "ponto_orvalho_medio_c,indice_calor_max_c,nivel_risco_max,dia_perigoso\n");
        LocalDate data = LocalDate.of(2024, 1, 1);
        for (int i = 0; i < 60; i++) {
            double indice = i % 2 == 0 ? 31.0 : (i % 4 == 1 ? 33.0 : 29.0);
            csv.append(data.plusDays(i)).append(",24,31.0,21.0,26.0,60,40,0,925,20000,2,17,").append(indice).append(",X,0\n");
        }
        csv.append("2024-03-01,5,,,,,,,,,,,,,\n"); // linha incompleta
        return new MockMultipartFile("arquivo", "inmet.csv", "text/csv", csv.toString().getBytes());
    }

    @BeforeEach
    void importar() throws Exception {
        mvc.perform(multipart("/estacoes/A701/observacoes/importar").file(csvSintetico()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.importadas").value(60))
                .andExpect(jsonPath("$.ignoradasIncompletas").value(1));
    }

    @Test
    @DisplayName("Reimportar o mesmo arquivo não duplica observações")
    void reimportacao() throws Exception {
        mvc.perform(multipart("/estacoes/A701/observacoes/importar").file(csvSintetico()))
                .andExpect(jsonPath("$.importadas").value(0))
                .andExpect(jsonPath("$.ignoradasJaExistentes").value(60));
    }

    @Test
    @DisplayName("CSV sem as colunas obrigatórias devolve 400")
    void csvInvalido() throws Exception {
        MockMultipartFile ruim = new MockMultipartFile("arquivo", "x.csv", "text/csv", "a,b\n1,2\n".getBytes());
        mvc.perform(multipart("/estacoes/A701/observacoes/importar").file(ruim))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Filtro somentePerigosos traz só dias com índice ≥ 32 °C")
    void filtroPerigosos() throws Exception {
        mvc.perform(get("/estacoes/A701/observacoes").param("inicio", "2024-01-01").param("fim", "2024-03-31")
                        .param("somentePerigosos", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(15))
                .andExpect(jsonPath("$.content[0].diaPerigoso").value(true));
    }

    @Test
    @DisplayName("POST observação calcula índice de calor e nível; repetir o dia devolve 409")
    void cadastrarObservacao() throws Exception {
        String corpo = "{\"data\":\"2024-03-10\",\"temperaturaMaxima\":34.0,\"temperaturaMinima\":22.0,\"umidadeMinima\":50}";
        mvc.perform(post("/estacoes/A701/observacoes").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.indiceCalorMaximo").value(38.4))
                .andExpect(jsonPath("$.nivelRisco").value("CAUTELA_EXTREMA"))
                .andExpect(jsonPath("$.origem").value("MANUAL"));
        mvc.perform(post("/estacoes/A701/observacoes").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST observação com temperaturas incoerentes devolve 400")
    void temperaturasIncoerentes() throws Exception {
        mvc.perform(post("/estacoes/A701/observacoes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"data\":\"2024-03-10\",\"temperaturaMaxima\":20,\"temperaturaMinima\":25,\"umidadeMinima\":50}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].field").value("temperaturasCoerentes"));
    }

    @Test
    @DisplayName("GET /previsao/amanha devolve o contrato usado pela API C#")
    void previsaoAmanha() throws Exception {
        mvc.perform(get("/previsao/amanha").param("estacao", "A701"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estacao").value("A701"))
                .andExpect(jsonPath("$.data").value("2024-03-01"))
                .andExpect(jsonPath("$.perigoso").isBoolean())
                .andExpect(jsonPath("$.probabilidade").isNumber());
    }

    @Test
    @DisplayName("POST previsão a partir de um dia de 31 °C: 15 dos 29 dias análogos tiveram dia seguinte perigoso")
    void gerarPrevisao() throws Exception {
        mvc.perform(post("/estacoes/A701/previsoes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dataBase\":\"2024-02-28\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dataPrevista").value("2024-02-29"))
                .andExpect(jsonPath("$.probabilidadePerigo").value(0.517))
                .andExpect(jsonPath("$.amostrasHistoricas").value(29))
                .andExpect(jsonPath("$.perigosa").value(true))
                .andExpect(jsonPath("$.metodo").value("FREQUENCIA_HISTORICA"));
        mvc.perform(get("/estacoes/A701/previsoes")).andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("Avaliação do modelo devolve a matriz de confusão e as métricas")
    void avaliacao() throws Exception {
        mvc.perform(get("/estacoes/A701/previsoes/avaliacao").param("inicio", "2024-01-15").param("fim", "2024-02-28"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diasAvaliados").isNumber())
                .andExpect(jsonPath("$.recall").isNumber());
    }

    @Test
    @DisplayName("Resumo anual conta 15 dias perigosos no CSV sintético")
    void resumoAnual() throws Exception {
        mvc.perform(get("/estacoes/A701/estatisticas/anual"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ano").value(2024))
                .andExpect(jsonPath("$[0].diasPerigosos").value(15));
    }
}
