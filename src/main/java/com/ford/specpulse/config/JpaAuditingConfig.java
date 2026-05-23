package com.ford.specpulse.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;


/**
 * O auditing default do Spring Data devolve LocalDateTime, mas as entidades
 * deste projeto guardam timestamps como OffsetDateTime (TIMESTAMP WITH TIME
 * ZONE no H2/Postgres). Esse provider sincroniza os tipos para que @CreatedDate
 * e @LastModifiedDate funcionem no save() das entidades.
 */
@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "fornecedorDataHora")
public class JpaAuditingConfig {

    @Bean
    public DateTimeProvider fornecedorDataHora() {
        return () -> Optional.of(OffsetDateTime.now(ZoneOffset.UTC));
    }
}
