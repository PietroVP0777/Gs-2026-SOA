package br.com.fiap.transitoclaro.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Linha de metro, trem ou BRT. */
@Entity
@Table(name = "linhas")
public class Linha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80, unique = true)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoTransporte tipo;

    @Column(nullable = false, length = 80)
    private String operadora;

    @Column(length = 30)
    private String cor;

    protected Linha() {
        // exigido pelo JPA
    }

    public Linha(String nome, TipoTransporte tipo, String operadora, String cor) {
        atualizar(nome, tipo, operadora, cor);
    }

    public void atualizar(String nome, TipoTransporte tipo, String operadora, String cor) {
        if (nome == null || nome.isBlank()) {
            throw new RegraDeNegocioException("O nome da linha e obrigatorio.");
        }
        this.nome = nome.trim();
        this.tipo = tipo;
        this.operadora = operadora.trim();
        this.cor = (cor == null || cor.isBlank()) ? null : cor.trim();
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public TipoTransporte getTipo() {
        return tipo;
    }

    public String getOperadora() {
        return operadora;
    }

    public String getCor() {
        return cor;
    }
}
