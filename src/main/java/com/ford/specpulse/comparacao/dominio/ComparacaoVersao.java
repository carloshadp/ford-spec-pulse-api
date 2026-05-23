package com.ford.specpulse.comparacao.dominio;

import com.ford.specpulse.versao.dominio.Versao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;


@Entity
@Table(name = "comparacao_versoes")
public class ComparacaoVersao {

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

    @Column(name = "ordem", nullable = false)
    private int ordem;

    protected ComparacaoVersao() {
    }

    public ComparacaoVersao(Comparacao comparacao, Versao versao, int ordem) {
        this.comparacao = comparacao;
        this.versao = versao;
        this.ordem = ordem;
    }

    public UUID getId() { return id; }
    public Comparacao getComparacao() { return comparacao; }
    public Versao getVersao() { return versao; }
    public int getOrdem() { return ordem; }
}
