package br.com.fiap3espg.heatsafe.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EstacaoControllerTest {
    @Autowired
    MockMvc mvc;

    private static final String NOVA_ESTACAO = """
            {"codigo":"a771","nome":"SAO PAULO - INTERLAGOS","cidade":"São Paulo","uf":"sp",
             "latitude":-23.724,"longitude":-46.677,"altitude":786}
            """;

    @Test
    @DisplayName("POST /estacoes cadastra, normaliza o código e devolve 201 com Location")
    void cadastrar() throws Exception {
        mvc.perform(post("/estacoes").contentType(MediaType.APPLICATION_JSON).content(NOVA_ESTACAO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/estacoes/A771")))
                .andExpect(jsonPath("$.codigo").value("A771"))
                .andExpect(jsonPath("$.uf").value("SP"))
                .andExpect(jsonPath("$.totalObservacoes").value(0));
    }

    @Test
    @DisplayName("POST /estacoes com código repetido devolve 409")
    void codigoDuplicado() throws Exception {
        mvc.perform(post("/estacoes").contentType(MediaType.APPLICATION_JSON).content(NOVA_ESTACAO))
                .andExpect(status().isCreated());
        mvc.perform(post("/estacoes").contentType(MediaType.APPLICATION_JSON).content(NOVA_ESTACAO))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /estacoes com dados inválidos devolve 400 com os campos")
    void dadosInvalidos() throws Exception {
        mvc.perform(post("/estacoes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"XYZ\",\"nome\":\"\",\"cidade\":\"SP\",\"uf\":\"SPP\",\"latitude\":200,\"longitude\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[?(@.field=='codigo')]").exists())
                .andExpect(jsonPath("$[?(@.field=='latitude')]").exists())
                .andExpect(jsonPath("$[?(@.field=='uf')]").exists());
    }

    @Test
    @DisplayName("GET /estacoes/A701 detalha a estação criada pela migração")
    void detalhar() throws Exception {
        mvc.perform(get("/estacoes/a701"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("SAO PAULO - MIRANTE"));
    }

    @Test
    @DisplayName("Estação desativada some da API (404)")
    void desativar() throws Exception {
        mvc.perform(delete("/estacoes/A701")).andExpect(status().isNoContent());
        mvc.perform(get("/estacoes/A701")).andExpect(status().isNotFound());
    }
}
