package br.com.fiap3espg.heatsafe.domain.observacao;

public class ObservacaoNotFoundException extends RuntimeException {
    public ObservacaoNotFoundException(String mensagem) {
        super(mensagem);
    }
}
