package com.ford.specpulse.autenticacao.dominio;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;


/**
 * Propriedades de configuracao do JWT carregadas via specpulse.jwt.* no
 * application.properties.
 */
@ConfigurationProperties(prefix = "specpulse.jwt")
public record PropriedadesJwt(

        @NotBlank @Size(min = 32) String segredo,

        @NotBlank String emissor,

        @NotNull @Positive Long expiraAcessoSegundos,

        @NotNull @Positive Long expiraRefreshSegundos
) {
}
