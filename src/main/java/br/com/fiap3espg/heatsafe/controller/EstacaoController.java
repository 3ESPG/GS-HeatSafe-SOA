package br.com.fiap3espg.heatsafe.controller;

import br.com.fiap3espg.heatsafe.domain.estacao.*;
import br.com.fiap3espg.heatsafe.service.EstacaoService;
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
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/estacoes")
@Tag(name = "Estações", description = "Estações meteorológicas que fornecem os dados de clima")
@RequiredArgsConstructor
public class EstacaoController {
    private final EstacaoService service;

    @PostMapping
    @Operation(summary = "Cadastra uma estação meteorológica")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Estação cadastrada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "409", description = "Código de estação já cadastrado")
    })
    public ResponseEntity<DadosDetalhamentoEstacao> cadastrarEstacao(
            @RequestBody @Valid DadosCadastroEstacao dados,
            UriComponentsBuilder uriBuilder) {
        DadosDetalhamentoEstacao dto = service.cadastrarEstacao(dados);
        URI uri = uriBuilder.path("/estacoes/{codigo}").buildAndExpand(dto.codigo()).toUri();
        return ResponseEntity.created(uri).body(dto);
    }

    @GetMapping
    @Operation(summary = "Lista as estações ativas (paginado)")
    @ApiResponse(responseCode = "200", description = "Página de estações")
    public ResponseEntity<Page<DadosListagemEstacao>> listarEstacoes(
            @ParameterObject @PageableDefault(size = 10, sort = "codigo") Pageable paginacao) {
        return ResponseEntity.ok(service.listarEstacoes(paginacao));
    }

    @GetMapping("/{codigo}")
    @Operation(summary = "Detalha uma estação, com o total de observações")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estação encontrada"),
            @ApiResponse(responseCode = "404", description = "Estação não encontrada ou inativa")
    })
    public ResponseEntity<DadosDetalhamentoEstacao> detalharEstacao(
            @Parameter(example = "A701") @PathVariable String codigo) {
        return ResponseEntity.ok(service.detalharEstacao(codigo));
    }

    @PutMapping("/{codigo}")
    @Operation(summary = "Atualiza os dados de uma estação (só os campos enviados)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estação atualizada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Estação não encontrada ou inativa")
    })
    public ResponseEntity<DadosDetalhamentoEstacao> atualizarEstacao(
            @Parameter(example = "A701") @PathVariable String codigo,
            @RequestBody @Valid DadosAtualizacaoEstacao dados) {
        return ResponseEntity.ok(service.atualizarEstacao(codigo, dados));
    }

    @DeleteMapping("/{codigo}")
    @Operation(summary = "Desativa uma estação (exclusão lógica)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Estação desativada"),
            @ApiResponse(responseCode = "404", description = "Estação não encontrada ou inativa")
    })
    public ResponseEntity<Void> desativarEstacao(@Parameter(example = "A701") @PathVariable String codigo) {
        service.desativarEstacao(codigo);
        return ResponseEntity.noContent().build();
    }
}
