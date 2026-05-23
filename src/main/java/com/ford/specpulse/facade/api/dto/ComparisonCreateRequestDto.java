package com.ford.specpulse.facade.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ComparisonCreateRequestDto(

        @NotBlank(message = "referenceVersionId é obrigatório")
        String referenceVersionId,

        @NotEmpty(message = "informe ao menos um competitorVersionId")
        @Size(min = 1, max = 3, message = "a comparação aceita entre 1 e 3 concorrentes")
        List<String> competitorVersionIds,

        List<String> attributeIds,

        String customerProfileId,

        String title,

        String description
) {
}
