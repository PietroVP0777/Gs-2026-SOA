package br.com.fiap.transitoclaro.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/** Resultado de uma verificacao do trajeto (historico). */
@Entity
@Table(name = "alertas")
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "trajeto_id", nullable = false)
    private Trajeto trajeto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private StatusLinha status;

    @Column(nullable = false, length = 300)
    private String mensagem;

    @Column(name = "linha_mais_afetada", length = 80)
    private String linhaMaisAfetada;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    protected Alerta() {
        // exigido pelo JPA
    }

    public Alerta(Trajeto trajeto, StatusLinha status, String mensagem, String linhaMaisAfetada,
                  LocalDateTime criadoEm) {
        this.trajeto = trajeto;
        this.status = status;
        this.mensagem = mensagem;
        this.linhaMaisAfetada = linhaMaisAfetada;
        this.criadoEm = criadoEm;
    }

    public Long getId() {
        return id;
    }

    public Trajeto getTrajeto() {
        return trajeto;
    }

    public StatusLinha getStatus() {
        return status;
    }

    public String getMensagem() {
        return mensagem;
    }

    public String getLinhaMaisAfetada() {
        return linhaMaisAfetada;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}
