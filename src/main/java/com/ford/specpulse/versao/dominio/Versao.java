package com.ford.specpulse.versao.dominio;

import com.ford.specpulse.compartilhado.EntidadeBase;
import com.ford.specpulse.veiculo.dominio.Veiculo;
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
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "versoes")
public class Versao extends EntidadeBase {

    @Id
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "veiculo_id", nullable = false)
    private Veiculo veiculo;

    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(name = "powertrain", nullable = false, length = 30)
    private Powertrain powertrain;

    @Column(name = "tracao", nullable = false, length = 10)
    private String tracao;

    @Column(name = "cabine", length = 20)
    private String cabine;

    @Column(name = "preco_sugerido", precision = 12, scale = 2)
    private BigDecimal precoSugerido;

    protected Versao() {
    }

    public Versao(Veiculo veiculo, String nome, Powertrain powertrain,
                  String tracao, String cabine, BigDecimal precoSugerido) {
        this.veiculo = Objects.requireNonNull(veiculo, "veiculo e obrigatorio");
        this.nome = Objects.requireNonNull(nome, "nome e obrigatorio");
        this.powertrain = Objects.requireNonNull(powertrain, "powertrain e obrigatorio");
        this.tracao = Objects.requireNonNull(tracao, "tracao e obrigatoria");
        this.cabine = cabine;
        this.precoSugerido = precoSugerido;
    }

    public UUID getId() {
        return id;
    }

    public Veiculo getVeiculo() {
        return veiculo;
    }

    public String getNome() {
        return nome;
    }

    public Powertrain getPowertrain() {
        return powertrain;
    }

    public String getTracao() {
        return tracao;
    }

    public String getCabine() {
        return cabine;
    }

    public BigDecimal getPrecoSugerido() {
        return precoSugerido;
    }
}
