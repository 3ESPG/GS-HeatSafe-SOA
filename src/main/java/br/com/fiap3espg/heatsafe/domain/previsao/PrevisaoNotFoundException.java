package br.com.fiap3espg.heatsafe.domain.previsao;

public class PrevisaoNotFoundException extends RuntimeException {
    public PrevisaoNotFoundException(Long id) {
        super("Previsão " + id + " não encontrada!");
    }
}
