package br.com.fiap3espg.heatsafe.domain.estacao;

import jakarta.validation.constraints.*;

public record DadosAtualizacaoEstacao(
        @Size(max = 100)
        String nome,

        @Size(max = 100)
        String cidade,

        @Pattern(regexp = "[A-Za-z]{2}", message = "UF deve ter 2 letras")
        String uf,

        @DecimalMin("-90.0") @DecimalMax("90.0")
        Double latitude,

        @DecimalMin("-180.0") @DecimalMax("180.0")
        Double longitude,

        @PositiveOrZero
        Double altitude) {
}
