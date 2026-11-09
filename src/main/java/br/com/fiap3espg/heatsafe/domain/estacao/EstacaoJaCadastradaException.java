package br.com.fiap3espg.heatsafe.domain.estacao;

public class EstacaoJaCadastradaException extends RuntimeException {
    public EstacaoJaCadastradaException(String codigo) {
        super("Já existe uma estação com o código " + codigo);
    }
}
