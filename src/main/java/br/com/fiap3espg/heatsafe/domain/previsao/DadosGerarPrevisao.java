package br.com.fiap3espg.heatsafe.domain.previsao;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record DadosGerarPrevisao(
        @Schema(description = "Dia observado usado como base. Se vazio, usa o último dia com dados da estação.",
                example = "2023-11-14")
        LocalDate dataBase) {
}
