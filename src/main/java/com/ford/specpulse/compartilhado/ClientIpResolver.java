package com.ford.specpulse.compartilhado;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Resolve o IP real do cliente atras do proxy do Render.
 *
 * O Render (proxy de borda) sempre acrescenta o IP real do cliente como o
 * ULTIMO endereco da cadeia X-Forwarded-For, apos qualquer valor que o
 * proprio cliente tenha enviado. Confiar no PRIMEIRO endereco (comportamento
 * anterior) permitia que qualquer requisicao forjasse o header e contornasse
 * o rate limit e o bloqueio por forca bruta, que sao aplicados por IP.
 */
public final class ClientIpResolver {

    private ClientIpResolver() {
    }

    public static String resolver(HttpServletRequest req) {
        String forwarded = req.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            String[] enderecos = forwarded.split(",");
            return enderecos[enderecos.length - 1].trim();
        }
        return req.getRemoteAddr();
    }
}
