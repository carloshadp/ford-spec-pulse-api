package com.ford.specpulse.compartilhado;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Resolucao do IP real por tras do proxy do Render")
class ClientIpResolverTest {

    @Test
    @DisplayName("sem X-Forwarded-For, usa o IP da conexao")
    void semHeader() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr("203.0.113.9");

        assertThat(ClientIpResolver.resolver(req)).isEqualTo("203.0.113.9");
    }

    @Test
    @DisplayName("com X-Forwarded-For, usa o ULTIMO endereco (o que o proxy acrescentou), nao o primeiro")
    void comHeaderForjado() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr("10.0.0.1");
        req.addHeader("X-Forwarded-For", "1.2.3.4, 203.0.113.9");

        assertThat(ClientIpResolver.resolver(req))
                .as("1.2.3.4 e forjado pelo cliente; 203.0.113.9 e o que o Render acrescentou")
                .isEqualTo("203.0.113.9");
    }
}
