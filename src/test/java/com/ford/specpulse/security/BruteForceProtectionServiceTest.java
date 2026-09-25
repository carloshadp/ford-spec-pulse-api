package com.ford.specpulse.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Protecao contra forca bruta no login")
class BruteForceProtectionServiceTest {

    private final BruteForceProtectionService servico = new BruteForceProtectionService();

    @Test
    @DisplayName("bloqueia o IP na 5a falha de login")
    void bloqueiaNaQuintaFalha() {
        for (int i = 0; i < 4; i++) {
            servico.registrarFalha("10.0.0.1", "ana@ford.internal");
        }
        assertThat(servico.estaBloqueado("10.0.0.1")).isFalse();

        servico.registrarFalha("10.0.0.1", "ana@ford.internal");
        assertThat(servico.estaBloqueado("10.0.0.1")).isTrue();
    }

    @Test
    @DisplayName("o bloqueio vale so para o IP que errou")
    void bloqueioPorIp() {
        for (int i = 0; i < 5; i++) {
            servico.registrarFalha("10.0.0.1", "ana@ford.internal");
        }
        assertThat(servico.estaBloqueado("10.0.0.2")).isFalse();
    }

    @Test
    @DisplayName("login bem-sucedido zera o contador de falhas")
    void sucessoZeraContador() {
        for (int i = 0; i < 4; i++) {
            servico.registrarFalha("10.0.0.1", "ana@ford.internal");
        }
        servico.registrarSucesso("10.0.0.1");
        servico.registrarFalha("10.0.0.1", "ana@ford.internal");

        assertThat(servico.estaBloqueado("10.0.0.1")).isFalse();
    }
}
