package com.ford.specpulse.autenticacao;

import com.fasterxml.jackson.databind.JsonNode;
import com.ford.specpulse.suporte.TesteIntegracaoBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Autenticacao: login, refresh, logout e registro")
class AutenticacaoTest extends TesteIntegracaoBase {

    @Test
    @DisplayName("login valido devolve access token, refresh token e dados do usuario")
    void loginValido() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paraJson(Map.of("email", "analista@ford.internal", "senha", "analista123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoToken").value("Bearer"))
                .andExpect(jsonPath("$.accessToken", not(emptyString())))
                .andExpect(jsonPath("$.refreshToken", not(emptyString())))
                .andExpect(jsonPath("$.expiraEm").exists())
                .andExpect(jsonPath("$.usuario.email").value("analista@ford.internal"))
                .andExpect(jsonPath("$.usuario.perfil").value("ANALISTA"))
                .andExpect(jsonPath("$.usuario.ativo").value(true));
    }

    @Test
    @DisplayName("login com senha errada e recusado com erro padronizado e sem token")
    void loginSenhaErrada() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paraJson(Map.of("email", "gerente@ford.internal", "senha", "senha-errada"))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("BUSINESS_RULE_VIOLATION"))
                .andExpect(jsonPath("$.message").value("Credenciais invalidas."))
                .andExpect(jsonPath("$.requestId", startsWith("req_")))
                .andExpect(jsonPath("$.accessToken").doesNotExist());
    }

    @Test
    @DisplayName("login com email em formato invalido devolve 400 apontando o campo")
    void loginEmailInvalido() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paraJson(Map.of("email", "nao-e-um-email", "senha", "qualquer"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details[0].field").value("email"));
    }

    @Test
    @DisplayName("refresh emite um novo par de tokens e o refresh antigo deixa de valer")
    void refreshRotacionaToken() throws Exception {
        JsonNode sessao = login("gerente@ford.internal", "gerente123");
        String refreshAntigo = sessao.get("refreshToken").asText();

        mvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paraJson(Map.of("refreshToken", refreshAntigo))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", not(emptyString())))
                .andExpect(jsonPath("$.refreshToken", not(refreshAntigo)));

        mvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paraJson(Map.of("refreshToken", refreshAntigo))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Refresh token expirado ou revogado."));
    }

    @Test
    @DisplayName("logout devolve 204 e revoga os refresh tokens do usuario")
    void logoutRevogaSessoes() throws Exception {
        JsonNode sessao = login("admin@ford.internal", "admin123");

        mvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + sessao.get("accessToken").asText()))
                .andExpect(status().isNoContent());

        mvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paraJson(Map.of("refreshToken", sessao.get("refreshToken").asText()))))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("logout sem token devolve 401")
    void logoutSemToken() throws Exception {
        mvc.perform(post("/api/auth/logout"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("registro publico cria usuario SOMENTE_LEITURA e ja devolve tokens (201)")
    void registroPublico() throws Exception {
        String email = "novo-" + UUID.randomUUID() + "@ford.internal";

        String corpo = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paraJson(Map.of("nome", "Usuario Novo", "email", email, "senha", "SenhaForte123"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuario.perfil").value("SOMENTE_LEITURA"))
                .andReturn().getResponse().getContentAsString();

        assertThat(json.readTree(corpo).get("accessToken").asText()).isNotBlank();
    }

    @Test
    @DisplayName("registro pedindo perfil ADMINISTRADOR sem ser admin e recusado")
    void registroEscalandoPerfil() throws Exception {
        String email = "intruso-" + UUID.randomUUID() + "@ford.internal";

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paraJson(Map.of("nome", "Intruso", "email", email,
                                "senha", "SenhaForte123", "perfil", "ADMINISTRADOR"))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("BUSINESS_RULE_VIOLATION"));
    }

    @Test
    @DisplayName("registro com senha curta devolve 400")
    void registroSenhaCurta() throws Exception {
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paraJson(Map.of("nome", "Teste", "email", "curta@ford.internal", "senha", "123"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0].field").value("senha"));
    }
}
