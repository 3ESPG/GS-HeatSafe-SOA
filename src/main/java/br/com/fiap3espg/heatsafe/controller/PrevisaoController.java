package br.com.fiap3espg.heatsafe.controller;

import br.com.fiap3espg.heatsafe.domain.previsao.DadosAvaliacaoModelo;
import br.com.fiap3espg.heatsafe.domain.previsao.DadosDetalhamentoPrevisao;
import br.com.fiap3espg.heatsafe.domain.previsao.DadosGerarPrevisao;
import br.com.fiap3espg.heatsafe.domain.previsao.DadosPrevisaoAmanha;
import br.com.fiap3espg.heatsafe.service.PrevisaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;

@RestController
@Tag(name = "Previsões", description = "Previsão de dia de calor perigoso para o dia seguinte")
@RequiredArgsConstructor
public class PrevisaoController {
    private final PrevisaoService service;

    @GetMapping("/previsao/amanha")
    @Operation(summary = "Amanhã será um dia perigoso? (contrato usado pela API C# do HeatSafe)",
            description = "Prevê o dia seguinte ao último dia observado da estação e salva a previsão.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Previsão gerada"),
            @ApiResponse(responseCode = "404", description = "Estação não encontrada ou sem observações")
    })
    public ResponseEntity<DadosPrevisaoAmanha> preverAmanha(
            @Parameter(example = "A701") @RequestParam(defaultValue = "A701") String estacao) {
        return ResponseEntity.ok(service.preverAmanha(estacao));
    }

    @PostMapping("/estacoes/{codigo}/previsoes")
    @Operation(summary = "Gera a previsão do dia seguinte a um dia observado")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Previsão gerada e salva"),
            @ApiResponse(responseCode = "400", description = "Histórico insuficiente"),
            @ApiResponse(responseCode = "404", description = "Estação ou dia base não encontrado")
    })
    public ResponseEntity<DadosDetalhamentoPrevisao> gerarPrevisao(
            @Parameter(example = "A701") @PathVariable String codigo,
            @RequestBody(required = false) DadosGerarPrevisao dados,
            UriComponentsBuilder uriBuilder) {
        DadosDetalhamentoPrevisao dto = service.gerarPrevisao(codigo, dados == null ? null : dados.dataBase());
        URI uri = uriBuilder.path("/previsoes/{id}").buildAndExpand(dto.id()).toUri();
        return ResponseEntity.created(uri).body(dto);
    }

    @GetMapping("/estacoes/{codigo}/previsoes")
    @Operation(summary = "Lista as previsões já geradas para a estação (paginado)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de previsões"),
            @ApiResponse(responseCode = "404", description = "Estação não encontrada")
    })
    public ResponseEntity<Page<DadosDetalhamentoPrevisao>> listarPrevisoes(
            @Parameter(example = "A701") @PathVariable String codigo,
            @ParameterObject @PageableDefault(size = 10, sort = "criadaEm", direction = Sort.Direction.DESC) Pageable paginacao) {
        return ResponseEntity.ok(service.listarPrevisoes(codigo, paginacao));
    }

    @GetMapping("/previsoes/{id}")
    @Operation(summary = "Detalha uma previsão")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Previsão encontrada"),
            @ApiResponse(responseCode = "404", description = "Previsão não encontrada")
    })
    public ResponseEntity<DadosDetalhamentoPrevisao> detalharPrevisao(@PathVariable Long id) {
        return ResponseEntity.ok(service.detalharPrevisao(id));
    }

    @GetMapping("/estacoes/{codigo}/previsoes/avaliacao")
    @Operation(summary = "Avalia o modelo em um período (recall, precisão, F1 e acurácia)",
            description = "Para cada dia, prevê o seguinte usando só o histórico anterior e compara com o real.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Métricas do modelo"),
            @ApiResponse(responseCode = "400", description = "Período inválido ou sem dados"),
            @ApiResponse(responseCode = "404", description = "Estação não encontrada")
    })
    public ResponseEntity<DadosAvaliacaoModelo> avaliarModelo(
            @Parameter(example = "A701") @PathVariable String codigo,
            @Parameter(example = "2023-01-01") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @Parameter(example = "2025-12-30") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @Parameter(description = "Limiar de probabilidade para testar (padrão: o configurado na aplicação)", example = "0.25")
            @RequestParam(required = false) Double limiar) {
        return ResponseEntity.ok(service.avaliarModelo(codigo, inicio, fim, limiar));
    }
}
