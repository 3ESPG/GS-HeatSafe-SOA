package br.com.fiap3espg.heatsafe.controller;

import br.com.fiap3espg.heatsafe.domain.ValidacaoException;
import br.com.fiap3espg.heatsafe.domain.observacao.DadosCadastroObservacao;
import br.com.fiap3espg.heatsafe.domain.observacao.DadosDetalhamentoObservacao;
import br.com.fiap3espg.heatsafe.domain.observacao.DadosResultadoImportacao;
import br.com.fiap3espg.heatsafe.service.ImportacaoObservacoesService;
import br.com.fiap3espg.heatsafe.service.ObservacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;

@RestController
@RequestMapping("/estacoes/{codigo}/observacoes")
@Tag(name = "Observações climáticas", description = "Resumo diário do clima de cada estação (dados reais do INMET)")
@RequiredArgsConstructor
public class ObservacaoController {
    private final ObservacaoService service;
    private final ImportacaoObservacoesService importacaoService;

    @PostMapping
    @Operation(summary = "Registra a observação de um dia",
            description = "O índice de calor máximo e o nível de risco são calculados pela API (fórmula NOAA).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Observação registrada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Estação não encontrada"),
            @ApiResponse(responseCode = "409", description = "Já existe observação para o dia")
    })
    public ResponseEntity<DadosDetalhamentoObservacao> cadastrarObservacao(
            @Parameter(example = "A701") @PathVariable String codigo,
            @RequestBody @Valid DadosCadastroObservacao dados,
            UriComponentsBuilder uriBuilder) {
        DadosDetalhamentoObservacao dto = service.cadastrarObservacao(codigo, dados);
        URI uri = uriBuilder.path("/estacoes/{codigo}/observacoes/{data}")
                .buildAndExpand(dto.estacao(), dto.data()).toUri();
        return ResponseEntity.created(uri).body(dto);
    }

    @PostMapping(value = "/importar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Importa observações de um CSV diário do INMET",
            description = "Formato do arquivo dados-inmet/inmet_a701_diario_2015_2025.csv. Dias já cadastrados são ignorados.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resumo da importação"),
            @ApiResponse(responseCode = "400", description = "Arquivo ausente ou com colunas inválidas"),
            @ApiResponse(responseCode = "404", description = "Estação não encontrada")
    })
    public ResponseEntity<DadosResultadoImportacao> importarCsv(
            @Parameter(example = "A701") @PathVariable String codigo,
            @RequestParam("arquivo") MultipartFile arquivo) throws IOException {
        if (arquivo.isEmpty()) {
            throw new ValidacaoException("Envie um arquivo CSV no campo 'arquivo'");
        }
        return ResponseEntity.ok(importacaoService.importar(codigo, arquivo.getInputStream()));
    }

    @GetMapping
    @Operation(summary = "Lista as observações de um período (paginado)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de observações"),
            @ApiResponse(responseCode = "400", description = "Período inválido"),
            @ApiResponse(responseCode = "404", description = "Estação não encontrada")
    })
    public ResponseEntity<Page<DadosDetalhamentoObservacao>> listarObservacoes(
            @Parameter(example = "A701") @PathVariable String codigo,
            @Parameter(example = "2023-11-01") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @Parameter(example = "2023-11-30") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @Parameter(description = "Se verdadeiro, traz só dias com índice de calor ≥ 32 °C")
            @RequestParam(defaultValue = "false") boolean somentePerigosos,
            @ParameterObject @PageableDefault(size = 31, sort = "data", direction = Sort.Direction.DESC) Pageable paginacao) {
        return ResponseEntity.ok(service.listarObservacoes(codigo, inicio, fim, somentePerigosos, paginacao));
    }

    @GetMapping("/{data}")
    @Operation(summary = "Detalha a observação de um dia")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Observação encontrada"),
            @ApiResponse(responseCode = "404", description = "Estação ou observação não encontrada")
    })
    public ResponseEntity<DadosDetalhamentoObservacao> detalharObservacao(
            @Parameter(example = "A701") @PathVariable String codigo,
            @Parameter(example = "2023-11-15") @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(service.detalharObservacao(codigo, data));
    }

    @DeleteMapping("/{data}")
    @Operation(summary = "Exclui a observação de um dia")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Observação excluída"),
            @ApiResponse(responseCode = "404", description = "Estação ou observação não encontrada")
    })
    public ResponseEntity<Void> excluirObservacao(
            @Parameter(example = "A701") @PathVariable String codigo,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        service.excluirObservacao(codigo, data);
        return ResponseEntity.noContent().build();
    }
}
