package com.ford.specpulse.veiculo.dominio;

import com.ford.specpulse.compartilhado.EntidadeBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.util.Objects;
import java.util.UUID;


@Entity
@Table(name = "marcas")
public class Marca extends EntidadeBase {

    @Id
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "nome", nullable = false, length = 120, unique = true)
    private String nome;

    @Column(name = "pais_origem", length = 60)
    private String paisOrigem;

    protected Marca() {
    }

    public Marca(String nome, String paisOrigem) {
        this.nome = Objects.requireNonNull(nome, "nome da marca e obrigatorio");
        this.paisOrigem = paisOrigem;
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getPaisOrigem() {
        return paisOrigem;
    }

    public void atualizarDados(String nome, String paisOrigem) {
        if (nome != null && !nome.isBlank()) {
            this.nome = nome;
        }
        this.paisOrigem = paisOrigem;
    }
}
