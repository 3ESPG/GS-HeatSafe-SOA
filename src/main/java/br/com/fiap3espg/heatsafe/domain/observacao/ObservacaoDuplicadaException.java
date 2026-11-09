package br.com.fiap3espg.heatsafe.domain.observacao;

import java.time.LocalDate;

public class ObservacaoDuplicadaException extends RuntimeException {
    public ObservacaoDuplicadaException(String estacao, LocalDate data) {
        super("Já existe observação da estação " + estacao + " para o dia " + data);
    }
}
