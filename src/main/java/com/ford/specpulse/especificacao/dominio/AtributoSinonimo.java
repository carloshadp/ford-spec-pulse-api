package com.ford.specpulse.especificacao.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.util.Objects;
import java.util.UUID;


@Entity
@Table(name = "atributo_sinonimos")
public class AtributoSinonimo {

    @Id
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "atributo_id", nullable = false)
    private AtributoDefinicao atributo;

    @Column(name = "termo", nullable = false, length = 120, unique = true)
    private String termo;

    protected AtributoSinonimo() {
    }

    public AtributoSinonimo(AtributoDefinicao atributo, String termo) {
        this.atributo = Objects.requireNonNull(atributo, "atributo e obrigatorio");
        this.termo = Objects.requireNonNull(termo, "termo e obrigatorio").toLowerCase();
    }

    public UUID getId() { return id; }
    public AtributoDefinicao getAtributo() { return atributo; }
    public String getTermo() { return termo; }
}
