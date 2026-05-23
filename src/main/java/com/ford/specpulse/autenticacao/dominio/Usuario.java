package com.ford.specpulse.autenticacao.dominio;

import com.ford.specpulse.compartilhado.EntidadeBase;
import com.ford.specpulse.seguranca.Perfil;
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
@Table(name = "usuarios")
public class Usuario extends EntidadeBase {

    @Id
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    @Column(name = "email", nullable = false, length = 160, unique = true)
    private String email;

    @Column(name = "senha_hash", nullable = false, length = 120)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "perfil", nullable = false, length = 40)
    private Perfil perfil;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    protected Usuario() {
    }

    public Usuario(String nome, String email, String senhaHash, Perfil perfil) {
        this.nome = Objects.requireNonNull(nome, "nome e obrigatorio");
        this.email = Objects.requireNonNull(email, "email e obrigatorio").toLowerCase();
        this.senhaHash = Objects.requireNonNull(senhaHash, "senha_hash e obrigatorio");
        this.perfil = Objects.requireNonNull(perfil, "perfil e obrigatorio");
        this.ativo = true;
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void alterarSenha(String novoHash) {
        this.senhaHash = Objects.requireNonNull(novoHash, "novo hash e obrigatorio");
    }

    public void alterarPerfil(Perfil novoPerfil) {
        this.perfil = Objects.requireNonNull(novoPerfil, "perfil e obrigatorio");
    }

    public void desativar() {
        this.ativo = false;
    }

    public void ativar() {
        this.ativo = true;
    }
}
