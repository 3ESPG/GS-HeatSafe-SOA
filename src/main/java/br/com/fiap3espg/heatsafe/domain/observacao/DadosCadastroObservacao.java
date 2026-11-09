package br.com.fiap3espg.heatsafe.domain.observacao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record DadosCadastroObservacao(
        @NotNull
        @PastOrPresent(message = "Não é possível registrar observação de uma data futura")
        LocalDate data,

        @NotNull
        @DecimalMin("-20.0") @DecimalMax("50.0")
        Double temperaturaMaxima,

        @NotNull
        @DecimalMin("-20.0") @DecimalMax("50.0")
        Double temperaturaMinima,

        @DecimalMin("-20.0") @DecimalMax("50.0")
        Double temperaturaMedia,

        @DecimalMin("0.0") @DecimalMax("100.0")
        Double umidadeMedia,

        @NotNull
        @DecimalMin("0.0") @DecimalMax("100.0")
        Double umidadeMinima,

        @PositiveOrZero
        Double precipitacao) {

    @JsonIgnore
    @AssertTrue(message = "A temperatura mínima não pode ser maior que a máxima")
    public boolean isTemperaturasCoerentes() {
        return temperaturaMaxima == null || temperaturaMinima == null || temperaturaMinima <= temperaturaMaxima;
    }
}
