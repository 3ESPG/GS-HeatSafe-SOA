package br.com.fiap3espg.heatsafe.domain.estacao;

import jakarta.validation.constraints.*;

public record DadosCadastroEstacao(
        @NotBlank
        @Pattern(regexp = "[A-Za-z]\\d{3}", message = "Código deve ter uma letra e 3 dígitos (padrão INMET, ex.: A701)")
        String codigo,

        @NotBlank
        @Size(max = 100)
        String nome,

        @NotBlank
        @Size(max = 100)
        String cidade,

        @NotBlank
        @Pattern(regexp = "[A-Za-z]{2}", message = "UF deve ter 2 letras")
        String uf,

        @NotNull
        @DecimalMin("-90.0") @DecimalMax("90.0")
        Double latitude,

        @NotNull
        @DecimalMin("-180.0") @DecimalMax("180.0")
        Double longitude,

        @PositiveOrZero
        Double altitude) {
}
