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

/**
 * Relato anonimo enviado por um passageiro (atraso, lotacao ou falha).
 * Nao guarda nenhum dado pessoal. A criacao passa por {@link #criar}, que garante as regras de negocio.
 */
@Entity
@Table(name = "relatos")
public class Relato {

    public static final int MAX_MINUTOS_ATRASO = 240;
    public static final int MAX_DESCRICAO = 280;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "linha_id", nullable = false)
    private Linha linha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoOcorrencia tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_lotacao", length = 20)
    private NivelLotacao nivelLotacao;

    @Column(name = "minutos_atraso")
    private Integer minutosAtraso;

    @Column(length = 280)
    private String descricao;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    protected Relato() {
        // exigido pelo JPA
    }

    public static Relato criar(Linha linha, TipoOcorrencia tipo, NivelLotacao nivelLotacao,
                               Integer minutosAtraso, String descricao, LocalDateTime agora) {
        if (linha == null) {
            throw new RegraDeNegocioException("A linha e obrigatoria.");
        }
        if (tipo == null) {
            throw new RegraDeNegocioException("O tipo da ocorrencia e obrigatorio.");
        }
        if (tipo == TipoOcorrencia.ATRASO) {
            if (minutosAtraso == null || minutosAtraso <= 0) {
                throw new RegraDeNegocioException("Para relatar atraso, informe os minutos de atraso (maior que zero).");
            }
            if (minutosAtraso > MAX_MINUTOS_ATRASO) {
                throw new RegraDeNegocioException("Atraso maximo aceito: " + MAX_MINUTOS_ATRASO + " minutos.");
            }
        }
        if (tipo == TipoOcorrencia.LOTACAO && nivelLotacao == null) {
            throw new RegraDeNegocioException("Para relatar lotacao, informe o nivel de lotacao.");
        }
        if (descricao != null && descricao.length() > MAX_DESCRICAO) {
            throw new RegraDeNegocioException("A descricao deve ter no maximo " + MAX_DESCRICAO + " caracteres.");
        }

        Relato r = new Relato();
        r.linha = linha;
        r.tipo = tipo;
        r.nivelLotacao = nivelLotacao;
        r.minutosAtraso = tipo == TipoOcorrencia.ATRASO ? minutosAtraso : null;
        r.descricao = (descricao == null || descricao.isBlank()) ? null : descricao.trim();
        r.criadoEm = agora;
        return r;
    }

    public Long getId() {
        return id;
    }

    public Linha getLinha() {
        return linha;
    }

    public TipoOcorrencia getTipo() {
        return tipo;
    }

    public NivelLotacao getNivelLotacao() {
        return nivelLotacao;
    }

    public Integer getMinutosAtraso() {
        return minutosAtraso;
    }

    public String getDescricao() {
        return descricao;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}
