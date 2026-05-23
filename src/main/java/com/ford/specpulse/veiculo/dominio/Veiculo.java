package com.ford.specpulse.veiculo.dominio;

import com.ford.specpulse.compartilhado.EntidadeBase;
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

import java.util.Objects;
import java.util.UUID;


@Entity
@Table(name = "veiculos")
public class Veiculo extends EntidadeBase {

    @Id
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "marca_id", nullable = false)
    private Marca marca;

    @Column(name = "modelo", nullable = false, length = 120)
    private String modelo;

    @Enumerated(EnumType.STRING)
    @Column(name = "segmento", nullable = false, length = 40)
    private Segmento segmento;

    @Enumerated(EnumType.STRING)
    @Column(name = "mercado", nullable = false, length = 20)
    private Mercado mercado;

    @Column(name = "ano_modelo", nullable = false)
    private Integer anoModelo;

    protected Veiculo() {
    }

    public Veiculo(Marca marca, String modelo, Segmento segmento, Mercado mercado, Integer anoModelo) {
        this.marca = Objects.requireNonNull(marca, "marca e obrigatoria");
        this.modelo = Objects.requireNonNull(modelo, "modelo e obrigatorio");
        this.segmento = Objects.requireNonNull(segmento, "segmento e obrigatorio");
        this.mercado = Objects.requireNonNull(mercado, "mercado e obrigatorio");
        this.anoModelo = Objects.requireNonNull(anoModelo, "ano do modelo e obrigatorio");
    }

    public UUID getId() {
        return id;
    }

    public Marca getMarca() {
        return marca;
    }

    public String getModelo() {
        return modelo;
    }

    public Segmento getSegmento() {
        return segmento;
    }

    public Mercado getMercado() {
        return mercado;
    }

    public Integer getAnoModelo() {
        return anoModelo;
    }
}
