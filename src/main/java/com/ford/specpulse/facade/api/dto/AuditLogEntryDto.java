package com.ford.specpulse.facade.api.dto;

import com.ford.specpulse.auditoria.dominio.Auditoria;

import java.time.OffsetDateTime;

public record AuditLogEntryDto(
        String id,
        String requestId,
        String userId,
        String action,
        String entity,
        String entityId,
        String ip,
        OffsetDateTime occurredAt
) {
    public static AuditLogEntryDto de(Auditoria a) {
        return new AuditLogEntryDto(
                a.getId().toString(),
                a.getRequestId(),
                a.getUsuarioId() != null ? a.getUsuarioId().toString() : null,
                a.getAcao(),
                a.getEntidade(),
                a.getEntidadeId(),
                a.getIp(),
                a.getOcorridoEm()
        );
    }
}
