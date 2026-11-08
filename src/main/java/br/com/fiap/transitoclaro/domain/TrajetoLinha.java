package br.com.fiap.transitoclaro.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Liga um trajeto a uma linha, com a ordem de uso. */
@Entity
@Table(name = "trajeto_linhas")
public class TrajetoLinha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trajeto_id", nullable = false)
    private Trajeto trajeto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "linha_id", nullable = false)
    private Linha linha;

    @Column(nullable = false)
    private int ordem;

    protected TrajetoLinha() {
        // exigido pelo JPA
    }

    TrajetoLinha(Trajeto trajeto, Linha linha, int ordem) {
        this.trajeto = trajeto;
        this.linha = linha;
        this.ordem = ordem;
    }

    public Long getId() {
        return id;
    }

    public Linha getLinha() {
        return linha;
    }

    public int getOrdem() {
        return ordem;
    }
}
