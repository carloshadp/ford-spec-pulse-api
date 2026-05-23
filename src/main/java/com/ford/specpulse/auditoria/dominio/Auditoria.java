package com.ford.specpulse.auditoria.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "auditoria")
public class Auditoria {

    @Id
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "request_id", length = 80)
    private String requestId;

    @Column(name = "usuario_id")
    private UUID usuarioId;

    @Column(name = "acao", nullable = false, length = 80)
    private String acao;

    @Column(name = "entidade", length = 80)
    private String entidade;

    @Column(name = "entidade_id", length = 80)
    private String entidadeId;

    @Column(name = "dados_json", columnDefinition = "TEXT")
    private String dadosJson;

    @Column(name = "ip", length = 50)
    private String ip;

    @Column(name = "ocorrido_em", nullable = false)
    private OffsetDateTime ocorridoEm;

    protected Auditoria() {
    }

    public Auditoria(String requestId, UUID usuarioId, String acao,
                     String entidade, String entidadeId, String dadosJson, String ip) {
        this.requestId = requestId;
        this.usuarioId = usuarioId;
        this.acao = acao;
        this.entidade = entidade;
        this.entidadeId = entidadeId;
        this.dadosJson = dadosJson;
        this.ip = ip;
        this.ocorridoEm = OffsetDateTime.now();
    }

    public UUID getId() { return id; }
    public String getRequestId() { return requestId; }
    public UUID getUsuarioId() { return usuarioId; }
    public String getAcao() { return acao; }
    public String getEntidade() { return entidade; }
    public String getEntidadeId() { return entidadeId; }
    public String getDadosJson() { return dadosJson; }
    public String getIp() { return ip; }
    public OffsetDateTime getOcorridoEm() { return ocorridoEm; }
}
