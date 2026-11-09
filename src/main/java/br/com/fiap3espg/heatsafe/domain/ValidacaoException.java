package br.com.fiap3espg.heatsafe.domain;

/** Violação de uma regra de negócio (responde 400 Bad Request). */
public class ValidacaoException extends RuntimeException {
    public ValidacaoException(String mensagem) {
        super(mensagem);
    }
}
