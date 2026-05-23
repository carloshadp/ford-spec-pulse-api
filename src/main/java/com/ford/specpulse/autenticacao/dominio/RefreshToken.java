package com.ford.specpulse.autenticacao.dominio;

import com.ford.specpulse.compartilhado.EntidadeBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;


/**
 * Token de atualizacao com identificador igual ao JTI do JWT. A linha so
 * existe enquanto o refresh continua valido; quando o cliente usa o token
 * gravamos `revogado_em` (rotacao) e geramos um novo.
 */
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken extends EntidadeBase {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "expira_em", nullable = false)
    private OffsetDateTime expiraEm;

    @Column(name = "revogado_em")
    private OffsetDateTime revogadoEm;

    @Column(name = "motivo_revogacao", length = 60)
    private String motivoRevogacao;

    protected RefreshToken() {
    }

    public RefreshToken(UUID jti, Usuario usuario, OffsetDateTime expiraEm) {
        this.id = Objects.requireNonNull(jti, "jti e obrigatorio");
        this.usuario = Objects.requireNonNull(usuario, "usuario e obrigatorio");
        this.expiraEm = Objects.requireNonNull(expiraEm, "expira_em e obrigatorio");
    }

    public UUID getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public OffsetDateTime getExpiraEm() {
        return expiraEm;
    }

    public OffsetDateTime getRevogadoEm() {
        return revogadoEm;
    }

    public String getMotivoRevogacao() {
        return motivoRevogacao;
    }

    public boolean isAtivo(OffsetDateTime momento) {
        return revogadoEm == null && expiraEm.isAfter(momento);
    }

    public void revogar(String motivo) {
        this.revogadoEm = OffsetDateTime.now();
        this.motivoRevogacao = motivo;
    }
}
