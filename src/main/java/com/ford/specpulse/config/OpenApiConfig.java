package com.ford.specpulse.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;


@Configuration
public class OpenApiConfig {

    private static final String NOME_ESQUEMA_SEGURANCA = "bearerAuth";

    @Value("${specpulse.openapi.servidor-producao:}")
    private String servidorProducao;

    @Bean
    public OpenAPI specPulseOpenAPI() {
        List<Server> servidores = new ArrayList<>();
        if (servidorProducao != null && !servidorProducao.isBlank()) {
            servidores.add(new Server().url(servidorProducao).description("Produção (Railway)"));
        }
        servidores.add(new Server().url("http://localhost:8080").description("Local dev"));

        return new OpenAPI()
                .info(new Info()
                        .title("Ford SpecPulse Twin API")
                        .description("""
                                API REST para analise competitiva de veiculos Ford frente a concorrentes.
                                Sustenta o frontend SpecPulse Twin (comparacoes, lacunas, valor percebido, recomendacoes).

                                **Autenticacao**: POST /api/auth/login → accessToken → Authorization: Bearer <token>.

                                **Namespace unico**: `/api` — endpoints em portugues, IDs slug estaveis, respostas paginadas.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("FIAP AOS — Sprint 1")
                                .email("carloshadp@gmail.com"))
                        .license(new License().name("Uso academico")))
                .servers(servidores)
                .addSecurityItem(new SecurityRequirement().addList(NOME_ESQUEMA_SEGURANCA))
                .components(new Components()
                        .addSecuritySchemes(NOME_ESQUEMA_SEGURANCA,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT HS256 — access token de 15 min emitido por POST /api/auth/login. "
                                                + "Inclua como 'Authorization: Bearer <accessToken>'. "
                                                + "Renove via POST /api/auth/refresh antes do vencimento.")));
    }
}
