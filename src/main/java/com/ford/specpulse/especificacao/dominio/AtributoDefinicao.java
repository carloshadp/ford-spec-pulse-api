package com.ford.specpulse.especificacao.dominio;

import com.ford.specpulse.compartilhado.EntidadeBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.util.Objects;
import java.util.UUID;


@Entity
@Table(name = "atributos_definicao")
public class AtributoDefinicao extends EntidadeBase {

    @Id
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "codigo_canonico", nullable = false, length = 80, unique = true)
    private String codigoCanonico;

    @Column(name = "nome_exibicao", nullable = false, length = 160)
    private String nomeExibicao;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false, length = 40)
    private Categoria categoria;

    @Column(name = "unidade", length = 20)
    private String unidade;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_dado", nullable = false, length = 15)
    private TipoDado tipoDado;

    @Enumerated(EnumType.STRING)
    @Column(name = "direcao_melhor", nullable = false, length = 15)
    private DirecaoMelhor direcaoMelhor;

    @Column(name = "descricao", length = 500)
    private String descricao;

    protected AtributoDefinicao() {
    }

    public AtributoDefinicao(String codigoCanonico, String nomeExibicao, Categoria categoria,
                             String unidade, TipoDado tipoDado, DirecaoMelhor direcaoMelhor,
                             String descricao) {
        this.codigoCanonico = Objects.requireNonNull(codigoCanonico, "codigoCanonico e obrigatorio");
        this.nomeExibicao = Objects.requireNonNull(nomeExibicao, "nomeExibicao e obrigatorio");
        this.categoria = Objects.requireNonNull(categoria, "categoria e obrigatoria");
        this.tipoDado = Objects.requireNonNull(tipoDado, "tipoDado e obrigatorio");
        this.direcaoMelhor = Objects.requireNonNull(direcaoMelhor, "direcaoMelhor e obrigatoria");
        this.unidade = unidade;
        this.descricao = descricao;
    }

    public UUID getId() { return id; }
    public String getCodigoCanonico() { return codigoCanonico; }
    public String getNomeExibicao() { return nomeExibicao; }
    public Categoria getCategoria() { return categoria; }
    public String getUnidade() { return unidade; }
    public TipoDado getTipoDado() { return tipoDado; }
    public DirecaoMelhor getDirecaoMelhor() { return direcaoMelhor; }
    public String getDescricao() { return descricao; }
}
