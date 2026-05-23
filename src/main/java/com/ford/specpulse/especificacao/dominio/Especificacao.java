package com.ford.specpulse.especificacao.dominio;

import com.ford.specpulse.compartilhado.EntidadeBase;
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
import java.util.Objects;
import java.util.UUID;


@Entity
@Table(name = "especificacoes")
public class Especificacao extends EntidadeBase {

    @Id
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "versao_id", nullable = false)
    private Versao versao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "atributo_id", nullable = false)
    private AtributoDefinicao atributo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fonte_id")
    private Fonte fonte;

    @Column(name = "valor_texto", length = 500)
    private String valorTexto;

    @Column(name = "valor_numero", precision = 18, scale = 4)
    private BigDecimal valorNumero;

    @Column(name = "valor_booleano")
    private Boolean valorBooleano;

    @Column(name = "unidade", length = 20)
    private String unidade;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 25)
    private StatusEspecificacao status;

    @Enumerated(EnumType.STRING)
    @Column(name = "confianca", nullable = false, length = 10)
    private NivelConfianca confianca;

    @Column(name = "data_captura")
    private OffsetDateTime dataCaptura;

    @Column(name = "observacao", length = 500)
    private String observacao;

    protected Especificacao() {
    }

    public Especificacao(Versao versao, AtributoDefinicao atributo, Fonte fonte,
                         String valorTexto, BigDecimal valorNumero, Boolean valorBooleano,
                         String unidade, StatusEspecificacao status, NivelConfianca confianca,
                         OffsetDateTime dataCaptura, String observacao) {
        this.versao = Objects.requireNonNull(versao, "versao e obrigatoria");
        this.atributo = Objects.requireNonNull(atributo, "atributo e obrigatorio");
        this.status = Objects.requireNonNull(status, "status e obrigatorio");
        this.confianca = Objects.requireNonNull(confianca, "confianca e obrigatoria");
        this.fonte = fonte;
        this.valorTexto = valorTexto;
        this.valorNumero = valorNumero;
        this.valorBooleano = valorBooleano;
        this.unidade = unidade;
        this.dataCaptura = dataCaptura;
        this.observacao = observacao;
    }

    public UUID getId() { return id; }
    public Versao getVersao() { return versao; }
    public AtributoDefinicao getAtributo() { return atributo; }
    public Fonte getFonte() { return fonte; }
    public String getValorTexto() { return valorTexto; }
    public BigDecimal getValorNumero() { return valorNumero; }
    public Boolean getValorBooleano() { return valorBooleano; }
    public String getUnidade() { return unidade; }
    public StatusEspecificacao getStatus() { return status; }
    public NivelConfianca getConfianca() { return confianca; }
    public OffsetDateTime getDataCaptura() { return dataCaptura; }
    public String getObservacao() { return observacao; }

    public boolean possuiValorConfirmado() {
        return status == StatusEspecificacao.CONFIRMADO;
    }
}
