package com.ford.specpulse.compartilhado;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Rate limit por IP")
class RateLimitFilterTest {

    private final RateLimitFilter filtro = new RateLimitFilter(3, 2);

    @Test
    @DisplayName("endpoints de autenticacao respondem 429 ao passar do limite")
    void limiteDeAutenticacao() throws Exception {
        assertThat(chamar("/api/auth/login", "10.0.0.1").getStatus()).isEqualTo(200);
        assertThat(chamar("/api/auth/login", "10.0.0.1").getStatus()).isEqualTo(200);

        MockHttpServletResponse bloqueada = chamar("/api/auth/login", "10.0.0.1");
        assertThat(bloqueada.getStatus()).isEqualTo(429);
        assertThat(bloqueada.getContentAsString()).contains("RATE_LIMIT_EXCEEDED");
    }

    @Test
    @DisplayName("demais endpoints tem limite proprio, separado do de autenticacao")
    void limiteGeralSeparado() throws Exception {
        chamar("/api/auth/login", "10.0.0.2");
        chamar("/api/auth/login", "10.0.0.2");
        assertThat(chamar("/api/auth/login", "10.0.0.2").getStatus()).isEqualTo(429);

        for (int i = 0; i < 3; i++) {
            assertThat(chamar("/api/marcas", "10.0.0.2").getStatus()).isEqualTo(200);
        }
        assertThat(chamar("/api/marcas", "10.0.0.2").getStatus()).isEqualTo(429);
    }

    @Test
    @DisplayName("o limite e contado por IP")
    void contagemPorIp() throws Exception {
        chamar("/api/auth/login", "10.0.0.3");
        chamar("/api/auth/login", "10.0.0.3");
        assertThat(chamar("/api/auth/login", "10.0.0.3").getStatus()).isEqualTo(429);

        assertThat(chamar("/api/auth/login", "10.0.0.4").getStatus()).isEqualTo(200);
    }

    @Test
    @DisplayName("cliente nao contorna o limite forjando X-Forwarded-For; so o IP do proxy conta")
    void naoContornaLimiteForjandoHeader() throws Exception {
        chamar("/api/auth/login", "10.0.0.5", "1.1.1.1");
        chamar("/api/auth/login", "10.0.0.5", "2.2.2.2");
        MockHttpServletResponse bloqueada = chamar("/api/auth/login", "10.0.0.5", "3.3.3.3");

        assertThat(bloqueada.getStatus())
                .as("mesmo trocando o X-Forwarded-For a cada chamada, o IP real (RemoteAddr) e o mesmo")
                .isEqualTo(429);
    }

    private MockHttpServletResponse chamar(String uri, String ip) throws Exception {
        return chamar(uri, ip, null);
    }

    private MockHttpServletResponse chamar(String uri, String ip, String forwardedForForjado) throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("POST", uri);
        req.setRemoteAddr(ip);
        if (forwardedForForjado != null) {
            req.addHeader("X-Forwarded-For", forwardedForForjado + ", " + ip);
        }
        MockHttpServletResponse resp = new MockHttpServletResponse();
        filtro.doFilter(req, resp, new MockFilterChain());
        return resp;
    }
}
