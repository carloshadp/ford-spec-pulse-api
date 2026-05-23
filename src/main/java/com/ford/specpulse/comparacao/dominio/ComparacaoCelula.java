package com.ford.specpulse.comparacao.dominio;

import com.ford.specpulse.especificacao.dominio.AtributoDefinicao;
import com.ford.specpulse.especificacao.dominio.NivelConfianca;
import com.ford.specpulse.especificacao.dominio.StatusEspecificacao;
import com.ford.specpulse.versao.dominio.Versao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;


@Entity
@Table(name = "comparacao_celulas")
public class ComparacaoCelula {

    @Id
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comparacao_id", nullable = false)
    private Comparacao comparacao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "versao_id", nullable = false)
    private Versao versao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "atributo_id", nullable = false)
    private AtributoDefinicao atributo;

    @Column(name = "valor_texto", length = 500)
    private String valorTexto;

    @Column(name = "valor_numero", precision = 18, scale = 4)
    private BigDecimal valorNumero;

    @Column(name = "valor_booleano")
    private Boolean valorBooleano;

    @Column(name = "unidade", length = 20)
    private String unidade;

    @Enumerated(EnumType.STRING)
    @Column(name = "confianca", nullable = false, length = 10)
    private NivelConfianca confianca;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_celula", nullable = false, length = 20)
    private StatusCelula statusCelula;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_dado", nullable = false, length = 25)
    private StatusEspecificacao statusDado;

    @Column(name = "fonte_nome_snapshot", length = 160)
    private String fonteNomeSnapshot;

    @Column(name = "data_snapshot", nullable = false)
    private OffsetDateTime dataSnapshot;

    protected ComparacaoCelula() {

    }

    public ComparacaoCelula(Comparacao comparacao, Versao versao, AtributoDefinicao atributo,
                            String valorTexto, BigDecimal valorNumero, Boolean valorBooleano,
                            String unidade, NivelConfianca confianca,
                            StatusCelula statusCelula, StatusEspecificacao statusDado,
                            String fonteNomeSnapshot) {
        this.comparacao = comparacao;
        this.versao = versao;
        this.atributo = atributo;
        this.valorTexto = valorTexto;
        this.valorNumero = valorNumero;
        this.valorBooleano = valorBooleano;
        this.unidade = unidade;
        this.confianca = confianca;
        this.statusCelula = statusCelula;
        this.statusDado = statusDado;
        this.fonteNomeSnapshot = fonteNomeSnapshot;
        this.dataSnapshot = OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public Comparacao getComparacao() { return comparacao; }
    public Versao getVersao() { return versao; }
    public AtributoDefinicao getAtributo() { return atributo; }
    public String getValorTexto() { return valorTexto; }
    public BigDecimal getValorNumero() { return valorNumero; }
    public Boolean getValorBooleano() { return valorBooleano; }
    public String getUnidade() { return unidade; }
    public NivelConfianca getConfianca() { return confianca; }
    public StatusCelula getStatusCelula() { return statusCelula; }
    public StatusEspecificacao getStatusDado() { return statusDado; }
    public String getFonteNomeSnapshot() { return fonteNomeSnapshot; }
    public OffsetDateTime getDataSnapshot() { return dataSnapshot; }
}
