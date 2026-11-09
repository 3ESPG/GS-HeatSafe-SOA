package br.com.fiap3espg.heatsafe.service;

import br.com.fiap3espg.heatsafe.domain.ValidacaoException;
import br.com.fiap3espg.heatsafe.domain.estacao.Estacao;
import br.com.fiap3espg.heatsafe.domain.observacao.DadosResultadoImportacao;
import br.com.fiap3espg.heatsafe.domain.observacao.ObservacaoClimatica;
import br.com.fiap3espg.heatsafe.domain.observacao.ObservacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Importa o CSV diário gerado a partir dos dados públicos do INMET
 * (dados-inmet/inmet_a701_diario_2015_2025.csv, produzido pelo script preparar_dados.py).
 * Linhas de dias já cadastrados ou sem os campos obrigatórios são ignoradas e contabilizadas.
 */
@Service
@RequiredArgsConstructor
public class ImportacaoObservacoesService {
    static final List<String> COLUNAS_OBRIGATORIAS = List.of(
            "data", "temp_max_c", "temp_min_c", "umidade_min_pct", "indice_calor_max_c");

    private final ObservacaoRepository repository;
    private final EstacaoService estacaoService;

    @Transactional
    public DadosResultadoImportacao importar(String codigoEstacao, InputStream csv) {
        Estacao estacao = estacaoService.buscarAtiva(codigoEstacao);
        Set<LocalDate> jaCadastradas = new HashSet<>(repository.listarDatasCadastradas(estacao));

        List<ObservacaoClimatica> novas = new ArrayList<>();
        int linhasLidas = 0;
        int incompletas = 0;
        int existentes = 0;

        try (BufferedReader leitor = new BufferedReader(new InputStreamReader(csv, StandardCharsets.UTF_8))) {
            Map<String, Integer> colunas = lerCabecalho(leitor.readLine());
            String linha;
            while ((linha = leitor.readLine()) != null) {
                if (linha.isBlank()) {
                    continue;
                }
                linhasLidas++;
                String[] campos = linha.split(",", -1);
                Optional<ObservacaoClimatica> observacao = converter(estacao, campos, colunas);
                if (observacao.isEmpty()) {
                    incompletas++;
                } else if (!jaCadastradas.add(observacao.get().getData())) {
                    existentes++;
                } else {
                    novas.add(observacao.get());
                }
            }
        } catch (IOException e) {
            throw new ValidacaoException("Não foi possível ler o arquivo CSV: " + e.getMessage());
        }

        repository.saveAll(novas);
        LocalDate primeira = novas.stream().map(ObservacaoClimatica::getData).min(LocalDate::compareTo).orElse(null);
        LocalDate ultima = novas.stream().map(ObservacaoClimatica::getData).max(LocalDate::compareTo).orElse(null);
        return new DadosResultadoImportacao(estacao.getCodigo(), linhasLidas, novas.size(), existentes, incompletas,
                primeira, ultima);
    }

    private Map<String, Integer> lerCabecalho(String cabecalho) {
        if (cabecalho == null || cabecalho.isBlank()) {
            throw new ValidacaoException("Arquivo CSV vazio");
        }
        Map<String, Integer> colunas = new HashMap<>();
        String[] nomes = cabecalho.replace("﻿", "").split(",");
        for (int i = 0; i < nomes.length; i++) {
            colunas.put(nomes[i].trim().toLowerCase(), i);
        }
        List<String> faltando = COLUNAS_OBRIGATORIAS.stream().filter(c -> !colunas.containsKey(c)).toList();
        if (!faltando.isEmpty()) {
            throw new ValidacaoException("CSV inválido: colunas obrigatórias ausentes " + faltando);
        }
        return colunas;
    }

    private Optional<ObservacaoClimatica> converter(Estacao estacao, String[] campos, Map<String, Integer> colunas) {
        try {
            LocalDate data = LocalDate.parse(valor(campos, colunas, "data"));
            Double maxima = numero(campos, colunas, "temp_max_c");
            Double minima = numero(campos, colunas, "temp_min_c");
            Double umidadeMinima = numero(campos, colunas, "umidade_min_pct");
            Double indiceCalor = numero(campos, colunas, "indice_calor_max_c");
            if (maxima == null || minima == null || umidadeMinima == null || indiceCalor == null) {
                return Optional.empty();
            }
            return Optional.of(ObservacaoClimatica.importada(estacao, data, maxima, minima,
                    numero(campos, colunas, "temp_media_c"), numero(campos, colunas, "umidade_media_pct"),
                    umidadeMinima, numero(campos, colunas, "precipitacao_mm"), indiceCalor));
        } catch (DateTimeParseException | NumberFormatException | ArrayIndexOutOfBoundsException e) {
            return Optional.empty();
        }
    }

    private static String valor(String[] campos, Map<String, Integer> colunas, String coluna) {
        Integer indice = colunas.get(coluna);
        return indice == null || indice >= campos.length ? "" : campos[indice].trim();
    }

    private static Double numero(String[] campos, Map<String, Integer> colunas, String coluna) {
        String texto = valor(campos, colunas, coluna);
        return texto.isEmpty() ? null : Double.parseDouble(texto);
    }
}
