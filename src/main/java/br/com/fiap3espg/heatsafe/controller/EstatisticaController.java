package br.com.fiap3espg.heatsafe.controller;

import br.com.fiap3espg.heatsafe.domain.estatistica.DadosResumoAnual;
import br.com.fiap3espg.heatsafe.domain.observacao.DadosDetalhamentoObservacao;
import br.com.fiap3espg.heatsafe.service.EstatisticaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/estacoes/{codigo}/estatisticas")
@Tag(name = "Estatísticas", description = "Indicadores do histórico de calor da estação")
@RequiredArgsConstructor
public class EstatisticaController {
    private final EstatisticaService service;

    @GetMapping("/anual")
    @Operation(summary = "Dias perigosos por ano (índice de calor ≥ 32 °C)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resumo por ano"),
            @ApiResponse(responseCode = "404", description = "Estação não encontrada")
    })
    public ResponseEntity<List<DadosResumoAnual>> resumoAnual(@Parameter(example = "A701") @PathVariable String codigo) {
        return ResponseEntity.ok(service.resumoAnual(codigo));
    }

    @GetMapping("/dias-mais-quentes")
    @Operation(summary = "Os 10 dias com maior índice de calor do histórico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista dos dias mais quentes"),
            @ApiResponse(responseCode = "404", description = "Estação não encontrada")
    })
    public ResponseEntity<List<DadosDetalhamentoObservacao>> diasMaisQuentes(
            @Parameter(example = "A701") @PathVariable String codigo) {
        return ResponseEntity.ok(service.diasMaisQuentes(codigo));
    }
}
