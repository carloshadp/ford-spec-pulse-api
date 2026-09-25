package com.ford.specpulse.suporte;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base dos testes de integracao: sobe a aplicacao inteira (Security, JPA, Flyway)
 * com o perfil "test" e expoe a API via MockMvc.
 *
 * Usuarios de seed (SeedUsuariosRunner): {usuario}@ford.internal / {usuario}123,
 * com usuario em leitor, analista, gerente, validador, admin.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class TesteIntegracaoBase {

    protected static final String VERSAO_RAPTOR =
            "version-ford-ranger-raptor-raptor-3-0-v6-biturbo-gasolina-4x4-cabine-dupla-2024";
    protected static final String VERSAO_HILUX =
            "version-toyota-hilux-srx-2-8-diesel-4x4-cabine-dupla-2024";
    protected static final String VERSAO_S10 =
            "version-chevrolet-s10-high-country-2-8-diesel-4x4-2024";

    private static final Map<String, String> TOKENS = new ConcurrentHashMap<>();

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper json;

    /** Faz login e devolve o corpo da resposta (tokens + usuario). */
    protected JsonNode login(String email, String senha) throws Exception {
        String corpo = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(Map.of("email", email, "senha", senha))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(corpo);
    }

    /** Header Authorization de um usuario de seed; o access token e reaproveitado entre testes. */
    protected String bearer(String usuario) {
        String token = TOKENS.computeIfAbsent(usuario, u -> {
            try {
                return login(u + "@ford.internal", u + "123").get("accessToken").asText();
            } catch (Exception e) {
                throw new IllegalStateException("Falha no login de " + u, e);
            }
        });
        return "Bearer " + token;
    }

    protected String paraJson(Object corpo) throws Exception {
        return json.writeValueAsString(corpo);
    }
}
