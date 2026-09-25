package com.ford.specpulse.autenticacao;

import com.fasterxml.jackson.databind.JsonNode;
import com.ford.specpulse.suporte.TesteIntegracaoBase;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("JWT: conteudo, expiracao e validacao do token")
class TokenJwtTest extends TesteIntegracaoBase {

    @Autowired
    private JwtDecoder decodificador;

    @Autowired
    private JwtEncoder codificador;

    @Test
    @DisplayName("access token traz sub, perfil, tipo e emissor, e expira em 15 minutos")
    void claimsDoAccessToken() throws Exception {
        JsonNode sessao = login("analista@ford.internal", "analista123");
        Jwt jwt = decodificador.decode(sessao.get("accessToken").asText());

        assertThat(jwt.getSubject()).isEqualTo(sessao.at("/usuario/id").asText());
        assertThat(jwt.getClaimAsString("role")).isEqualTo("ANALISTA");
        assertThat(jwt.getClaimAsString("type")).isEqualTo("access");
        assertThat(jwt.getClaimAsString("email")).isEqualTo("analista@ford.internal");
        assertThat(jwt.getClaimAsString("iss")).isEqualTo("ford-spec-pulse-api");
        assertThat(Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt())).isEqualTo(Duration.ofMinutes(15));
    }

    @Test
    @DisplayName("refresh token e do tipo refresh, nao carrega perfil e expira em 7 dias")
    void claimsDoRefreshToken() throws Exception {
        JsonNode sessao = login("leitor@ford.internal", "leitor123");
        Jwt jwt = decodificador.decode(sessao.get("refreshToken").asText());

        assertThat(jwt.getClaimAsString("type")).isEqualTo("refresh");
        assertThat(jwt.hasClaim("role")).isFalse();
        assertThat(Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt())).isEqualTo(Duration.ofDays(7));
    }

    @Test
    @DisplayName("token valido libera o recurso e a API reconhece o usuario pelas claims")
    void tokenValido() throws Exception {
        mvc.perform(get("/api/usuarios/me").header("Authorization", bearer("gerente")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("gerente@ford.internal"))
                .andExpect(jsonPath("$.roles[0]").value("manager"));
    }

    @Test
    @DisplayName("requisicao sem token devolve 401 no formato de erro padrao")
    void semToken() throws Exception {
        mvc.perform(get("/api/marcas"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/api/marcas"));
    }

    @Test
    @DisplayName("token malformado devolve 401")
    void tokenMalformado() throws Exception {
        mvc.perform(get("/api/marcas").header("Authorization", "Bearer isto.nao.e-um-jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("controle: token montado com o segredo da API e datas validas e aceito")
    void tokenMontadoValido() throws Exception {
        String valido = emitir(codificador, Instant.now(), Instant.now().plus(Duration.ofMinutes(15)));

        mvc.perform(get("/api/marcas").header("Authorization", "Bearer " + valido))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("token expirado devolve 401")
    void tokenExpirado() throws Exception {
        Instant duasHorasAtras = Instant.now().minus(Duration.ofHours(2));
        String expirado = emitir(codificador, duasHorasAtras, duasHorasAtras.plus(Duration.ofMinutes(15)));

        mvc.perform(get("/api/marcas").header("Authorization", "Bearer " + expirado))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("token assinado com outro segredo devolve 401")
    void tokenAssinaturaInvalida() throws Exception {
        SecretKeySpec outraChave = new SecretKeySpec(
                "um-segredo-diferente-com-mais-de-32-caracteres".getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        JwtEncoder falsificador = new NimbusJwtEncoder(new ImmutableSecret<>(outraChave));
        String forjado = emitir(falsificador, Instant.now(), Instant.now().plus(Duration.ofMinutes(15)));

        mvc.perform(get("/api/marcas").header("Authorization", "Bearer " + forjado))
                .andExpect(status().isUnauthorized());
    }

    /** Monta um access token de ADMINISTRADOR com o encoder e as datas informadas. */
    private static String emitir(JwtEncoder encoder, Instant emitidoEm, Instant expiraEm) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("ford-spec-pulse-api")
                .subject(UUID.randomUUID().toString())
                .issuedAt(emitidoEm)
                .expiresAt(expiraEm)
                .claim("type", "access")
                .claim("role", "ADMINISTRADOR")
                .build();
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
    }
}
