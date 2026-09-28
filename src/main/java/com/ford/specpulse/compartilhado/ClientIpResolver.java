package com.ford.specpulse.compartilhado;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Extrai o IP real do cliente, considerando proxies reversos via X-Forwarded-For.
 */
public final class ClientIpResolver {

    private ClientIpResolver() {
    }

    public static String resolver(HttpServletRequest req) {
        String forwarded = req.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return req.getRemoteAddr();
    }
}
