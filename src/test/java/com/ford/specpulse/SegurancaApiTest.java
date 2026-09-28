package com.ford.specpulse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de integração — controle de acesso (401, 403, RBAC).
 *
 * Sprint 3 AOS — FIAP 2026
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "specpulse.rate-limit.auth=500",
        "specpulse.rate-limit.geral=1000"
})
@DisplayName("Seguranca — controle de acesso")
class SegurancaApiTest {

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

    private String obterToken(String email, String senha) {
        String body = """
                {"email":"%s","senha":"%s"}""".formatted(email, senha);
        ResponseEntity<Map> resp = rest.postForEntity(
                baseUrl() + "/api/auth/login",
                new HttpEntity<>(body, jsonHeaders()),
                Map.class);
        assertThat(resp.getStatusCode())
                .as("Login de %s deve retornar 200".formatted(email))
                .isEqualTo(HttpStatus.OK);
        return (String) resp.getBody().get("accessToken");
    }

    // -----------------------------------------------------------------------
    // Acesso sem token — deve retornar 401
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/marcas sem token retorna 401")
    void marcasSemToken() {
        ResponseEntity<Map> resp = rest.getForEntity(
                baseUrl() + "/api/marcas", Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("GET /api/veiculos sem token retorna 401")
    void veiculosSemToken() {
        ResponseEntity<Map> resp = rest.getForEntity(
                baseUrl() + "/api/veiculos", Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("GET /api/usuarios sem token retorna 401")
    void usuariosSemToken() {
        ResponseEntity<Map> resp = rest.getForEntity(
                baseUrl() + "/api/usuarios", Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // -----------------------------------------------------------------------
    // Acesso com token valido mas sem permissao — deve retornar 403
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/usuarios com perfil SOMENTE_LEITURA retorna 403")
    void listarUsuariosPerfilLeitura() {
        String token = obterToken("leitor@ford.internal", "leitor123");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<Map> resp = rest.exchange(
                baseUrl() + "/api/usuarios",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("POST /api/comparacoes com perfil SOMENTE_LEITURA retorna 403")
    void criarComparacaoPerfilLeitura() {
        String token = obterToken("leitor@ford.internal", "leitor123");

        String body = """
                {"referenceVersionId":"qualquer","competitorVersionIds":["outro"],"title":"Teste"}""";
        HttpHeaders headers = jsonHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<Map> resp = rest.postForEntity(
                baseUrl() + "/api/comparacoes",
                new HttpEntity<>(body, headers),
                Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @DisplayName("GET /api/admin/registro-auditoria com perfil GERENTE retorna 403")
    void auditLogPerfilGerente() {
        String token = obterToken("gerente@ford.internal", "gerente123");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<Map> resp = rest.exchange(
                baseUrl() + "/api/admin/registro-auditoria",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    // -----------------------------------------------------------------------
    // Acesso com token valido e permissao correta — deve retornar 2xx
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/marcas com token valido (qualquer perfil) retorna 200")
    void marcasComToken() {
        String token = obterToken("leitor@ford.internal", "leitor123");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<Map> resp = rest.exchange(
                baseUrl() + "/api/marcas",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).containsKey("data");
    }

    @Test
    @DisplayName("GET /api/usuarios com perfil ADMINISTRADOR retorna 200")
    void listarUsuariosAdmin() {
        String token = obterToken("admin@ford.internal", "admin123");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<Map> resp = rest.exchange(
                baseUrl() + "/api/usuarios",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("GET /api/usuarios/me com token valido retorna 200 e dados do usuario")
    void perfilProprio() {
        String token = obterToken("analista@ford.internal", "analista123");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<Map> resp = rest.exchange(
                baseUrl() + "/api/usuarios/me",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // -----------------------------------------------------------------------
    // Endpoints publicos — acessiveis sem token
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("GET /actuator/health retorna 200 sem autenticacao")
    void healthPublico() {
        ResponseEntity<Map> resp = rest.getForEntity(
                baseUrl() + "/actuator/health", Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("GET /v3/api-docs retorna 200 sem autenticacao")
    void swaggerPublico() {
        ResponseEntity<Map> resp = rest.getForEntity(
                baseUrl() + "/v3/api-docs", Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    // -----------------------------------------------------------------------
    // Token invalido — deve retornar 401
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("GET /api/marcas com token JWT invalido retorna 401")
    void marcasTokenInvalido() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("token.invalido.qualquer");
        ResponseEntity<Map> resp = rest.exchange(
                baseUrl() + "/api/marcas",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
