package com.ford.specpulse.facade.api.dto;

import java.time.OffsetDateTime;

public record BackupRespostaDto(
        String arquivo,
        OffsetDateTime geradoEm
) {
}
