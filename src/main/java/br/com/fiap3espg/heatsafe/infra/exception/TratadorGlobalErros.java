package br.com.fiap3espg.heatsafe.infra.exception;

import br.com.fiap3espg.heatsafe.domain.ValidacaoException;
import br.com.fiap3espg.heatsafe.domain.estacao.EstacaoJaCadastradaException;
import br.com.fiap3espg.heatsafe.domain.estacao.EstacaoNotFoundException;
import br.com.fiap3espg.heatsafe.domain.observacao.ObservacaoDuplicadaException;
import br.com.fiap3espg.heatsafe.domain.observacao.ObservacaoNotFoundException;
import br.com.fiap3espg.heatsafe.domain.previsao.PrevisaoNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.List;

@RestControllerAdvice
public class TratadorGlobalErros {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<DadosBadRequest>> tratarBadRequest(MethodArgumentNotValidException ex) {
        List<DadosBadRequest> erros = ex.getFieldErrors().stream().map(DadosBadRequest::new).toList();
        return ResponseEntity.badRequest().body(erros);
    }

    @ExceptionHandler({EstacaoNotFoundException.class, ObservacaoNotFoundException.class, PrevisaoNotFoundException.class})
    public ResponseEntity<DadosMessage> tratarNaoEncontrado(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new DadosMessage(ex.getMessage()));
    }

    @ExceptionHandler({EstacaoJaCadastradaException.class, ObservacaoDuplicadaException.class})
    public ResponseEntity<DadosMessage> tratarConflito(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new DadosMessage(ex.getMessage()));
    }

    @ExceptionHandler(ValidacaoException.class)
    public ResponseEntity<DadosMessage> tratarValidacao(ValidacaoException ex) {
        return ResponseEntity.badRequest().body(new DadosMessage(ex.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<DadosMessage> tratarCorpoInvalido() {
        return ResponseEntity.badRequest().body(new DadosMessage("Corpo da requisição inválido ou mal formatado (JSON)"));
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class,
            MissingServletRequestPartException.class})
    public ResponseEntity<DadosMessage> tratarParametroInvalido(Exception ex) {
        return ResponseEntity.badRequest().body(new DadosMessage("Parâmetro ausente ou inválido: " + ex.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<DadosMessage> tratarIntegridade() {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new DadosMessage("Violação de integridade: registro duplicado ou em uso."));
    }

    public record DadosBadRequest(String field, String message) {
        public DadosBadRequest(FieldError erro) {
            this(erro.getField(), erro.getDefaultMessage());
        }
    }

    public record DadosMessage(String message) {
    }
}
