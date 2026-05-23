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
@Table(name = "fontes")
public class Fonte extends EntidadeBase {

    @Id
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "nome", nullable = false, length = 160)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 40)
    private TipoFonte tipo;

    @Column(name = "url", length = 500)
    private String url;

    @Column(name = "descricao", length = 500)
    private String descricao;

    protected Fonte() {
    }

    public Fonte(String nome, TipoFonte tipo, String url, String descricao) {
        this.nome = Objects.requireNonNull(nome, "nome e obrigatorio");
        this.tipo = Objects.requireNonNull(tipo, "tipo e obrigatorio");
        this.url = url;
        this.descricao = descricao;
    }

    public UUID getId() { return id; }
    public String getNome() { return nome; }
    public TipoFonte getTipo() { return tipo; }
    public String getUrl() { return url; }
    public String getDescricao() { return descricao; }
}
