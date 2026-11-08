package br.com.fiap.transitoclaro.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Trajeto diario de um passageiro (ex.: casa -> faculdade) com as linhas que ele usa.
 * Nao guarda dados pessoais: "apelido" e so um identificador livre escolhido pelo passageiro.
 */
@Entity
@Table(name = "trajetos")
public class Trajeto {

    public static final int MAX_LINHAS = 6;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String nome;

    @Column(nullable = false, length = 40)
    private String apelido;

    @Column(name = "horario_saida", nullable = false)
    private LocalTime horarioSaida;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @OneToMany(mappedBy = "trajeto", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem ASC")
    private List<TrajetoLinha> linhas = new ArrayList<>();

    @OneToMany(mappedBy = "trajeto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Alerta> alertas = new ArrayList<>();

    protected Trajeto() {
        // exigido pelo JPA
    }

    public Trajeto(String nome, String apelido, LocalTime horarioSaida, boolean ativo) {
        atualizarDados(nome, apelido, horarioSaida, ativo);
    }

    public void atualizarDados(String nome, String apelido, LocalTime horarioSaida, boolean ativo) {
        this.nome = nome;
        this.apelido = apelido;
        this.horarioSaida = horarioSaida;
        this.ativo = ativo;
    }

    /** Substitui as linhas do trajeto (na ordem recebida), garantindo as regras de negocio. */
    public void definirLinhas(List<Linha> novasLinhas) {
        if (novasLinhas == null || novasLinhas.isEmpty()) {
            throw new RegraDeNegocioException("O trajeto precisa ter ao menos uma linha.");
        }
        if (novasLinhas.size() > MAX_LINHAS) {
            throw new RegraDeNegocioException("Um trajeto pode ter no maximo " + MAX_LINHAS + " linhas.");
        }
        var vistos = new HashSet<Object>();
        for (Linha linha : novasLinhas) {
            Object chave = linha.getId() != null ? linha.getId() : linha.getNome();
            if (!vistos.add(chave)) {
                throw new RegraDeNegocioException("O trajeto contem linhas repetidas.");
            }
        }

        this.linhas.clear();
        int ordem = 1;
        for (Linha linha : novasLinhas) {
            this.linhas.add(new TrajetoLinha(this, linha, ordem++));
        }
    }

    @PrePersist
    void aoCriar() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now(ZoneOffset.UTC);
        }
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getApelido() {
        return apelido;
    }

    public LocalTime getHorarioSaida() {
        return horarioSaida;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public List<TrajetoLinha> getLinhas() {
        return linhas;
    }
}
