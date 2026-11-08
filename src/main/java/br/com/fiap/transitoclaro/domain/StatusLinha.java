package br.com.fiap.transitoclaro.domain;

public enum StatusLinha {
    NORMAL(0), MODERADO(1), CRITICO(2);

    private final int gravidade;

    StatusLinha(int gravidade) {
        this.gravidade = gravidade;
    }

    public int getGravidade() {
        return gravidade;
    }
}
