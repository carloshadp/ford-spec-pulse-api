package com.ford.specpulse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integração — fluxo de autenticação (login, registro, refresh, logout).
 *
 * Sprint 3 AOS — FIAP 2026
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "specpulse.rate-limit.auth=500",
        "specpulse.rate-limit.geral=1000"
})
@DisplayName("Autenticacao API")
class AutenticacaoApiTest {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    // -----------------------------------------------------------------------
    // Login
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/auth/login com credenciais corretas retorna 200 e tokens")
    void loginSucesso() {
        String body = """
                {"email":"gerente@ford.internal","senha":"gerente123"}""";

        ResponseEntity<Map> resp = rest.postForEntity(
                baseUrl() + "/api/auth/login",
                new HttpEntity<>(body, jsonHeaders()),
                Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).containsKey("accessToken");
        assertThat(resp.getBody()).containsKey("refreshToken");
        assertThat(resp.getBody().get("accessToken")).isNotNull();
    }

    @Test
    @DisplayName("POST /api/auth/login com senha errada retorna 422")
    void loginSenhaErrada() {
        String body = """
                {"email":"gerente@ford.internal","senha":"senhaerrada"}""";

        ResponseEntity<Map> resp = rest.postForEntity(
                baseUrl() + "/api/auth/login",
                new HttpEntity<>(body, jsonHeaders()),
                Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    @DisplayName("POST /api/auth/login com email inexistente retorna 422")
    void loginEmailInexistente() {
        String body = """
                {"email":"naoexiste@ford.internal","senha":"qualquer"}""";

        ResponseEntity<Map> resp = rest.postForEntity(
                baseUrl() + "/api/auth/login",
                new HttpEntity<>(body, jsonHeaders()),
                Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    @DisplayName("POST /api/auth/login com email em branco retorna 400")
    void loginEmailEmBranco() {
        String body = """
                {"email":"","senha":"gerente123"}""";

        ResponseEntity<Map> resp = rest.postForEntity(
                baseUrl() + "/api/auth/login",
                new HttpEntity<>(body, jsonHeaders()),
                Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("POST /api/auth/login com email invalido retorna 400")
    void loginEmailInvalido() {
        String body = """
                {"email":"nao-e-um-email","senha":"gerente123"}""";

        ResponseEntity<Map> resp = rest.postForEntity(
                baseUrl() + "/api/auth/login",
                new HttpEntity<>(body, jsonHeaders()),
                Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // -----------------------------------------------------------------------
    // Registro
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/auth/register com dados validos retorna 201 e tokens")
    void registroSucesso() {
        String body = """
                {"nome":"Novo Usuario","email":"novo.usuario.%d@ford.test","senha":"senha123"}"""
                .formatted(System.currentTimeMillis());

        ResponseEntity<Map> resp = rest.postForEntity(
                baseUrl() + "/api/auth/register",
                new HttpEntity<>(body, jsonHeaders()),
                Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resp.getBody()).containsKey("accessToken");
    }

    @Test
    @DisplayName("POST /api/auth/register com email ja cadastrado retorna 422")
    void registroEmailDuplicado() {
        String body = """
                {"nome":"Leitor Duplicado","email":"leitor@ford.internal","senha":"outropassword"}""";

        ResponseEntity<Map> resp = rest.postForEntity(
                baseUrl() + "/api/auth/register",
                new HttpEntity<>(body, jsonHeaders()),
                Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    // -----------------------------------------------------------------------
    // Refresh
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/auth/refresh com token valido retorna 200 e novo par de tokens")
    void refreshSucesso() {
        String loginBody = """
                {"email":"analista@ford.internal","senha":"analista123"}""";
        ResponseEntity<Map> loginResp = rest.postForEntity(
                baseUrl() + "/api/auth/login",
                new HttpEntity<>(loginBody, jsonHeaders()),
                Map.class);
        String refreshToken = (String) loginResp.getBody().get("refreshToken");

        String refreshBody = """
                {"refreshToken":"%s"}""".formatted(refreshToken);
        ResponseEntity<Map> resp = rest.postForEntity(
                baseUrl() + "/api/auth/refresh",
                new HttpEntity<>(refreshBody, jsonHeaders()),
                Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).containsKey("accessToken");
        assertThat(resp.getBody()).containsKey("refreshToken");
    }

    @Test
    @DisplayName("POST /api/auth/refresh com token invalido retorna 422")
    void refreshTokenInvalido() {
        String body = """
                {"refreshToken":"token.invalido.qualquer"}""";

        ResponseEntity<Map> resp = rest.postForEntity(
                baseUrl() + "/api/auth/refresh",
                new HttpEntity<>(body, jsonHeaders()),
                Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    // -----------------------------------------------------------------------
    // Logout
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("POST /api/auth/logout com token valido retorna 204")
    void logoutSucesso() {
        String loginBody = """
                {"email":"validador@ford.internal","senha":"validador123"}""";
        ResponseEntity<Map> loginResp = rest.postForEntity(
                baseUrl() + "/api/auth/login",
                new HttpEntity<>(loginBody, jsonHeaders()),
                Map.class);
        String accessToken = (String) loginResp.getBody().get("accessToken");

        HttpHeaders headers = jsonHeaders();
        headers.setBearerAuth(accessToken);
        ResponseEntity<Void> resp = rest.postForEntity(
                baseUrl() + "/api/auth/logout",
                new HttpEntity<>("", headers),
                Void.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    // logoutSemToken: testado em SegurancaApiTest via endpoints GET (401 geral)
    // Omitido aqui pois HttpURLConnection bloqueia leitura de 401 em POST streaming mode.
}
