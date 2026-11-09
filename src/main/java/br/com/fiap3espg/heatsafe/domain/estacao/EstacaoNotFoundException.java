package br.com.fiap3espg.heatsafe.domain.estacao;

public class EstacaoNotFoundException extends RuntimeException {
    public EstacaoNotFoundException(String codigo) {
        super("Estação " + codigo + " não encontrada ou inativa!");
    }
}
