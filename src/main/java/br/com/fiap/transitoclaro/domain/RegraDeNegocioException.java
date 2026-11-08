package br.com.fiap.transitoclaro.domain;

/** Violacao de regra de negocio (HTTP 400). */
public class RegraDeNegocioException extends RuntimeException {
    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
