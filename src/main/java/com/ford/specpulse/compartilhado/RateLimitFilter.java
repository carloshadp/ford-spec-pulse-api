package com.ford.specpulse.compartilhado;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate limiting simples por IP usando janela deslizante de 1 minuto.
 * Limites: 60 req/min geral, 10 req/min nos endpoints /api/auth/*.
 */
@Component
@Order(1)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);
    private static final long JANELA_MS = 60_000L;
    private static final int LIMITE_GERAL = 60;
    private static final int LIMITE_AUTH = 10;

    private final ConcurrentHashMap<String, WindowEntry> janelas = new ConcurrentHashMap<>();

    private record WindowEntry(long inicioJanela, AtomicInteger contagem) {
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws ServletException, IOException {
        String ip = obterIp(req);
        boolean ehAuth = req.getRequestURI().startsWith("/api/auth/");
        int limite = ehAuth ? LIMITE_AUTH : LIMITE_GERAL;

        String chave = ip + (ehAuth ? ":auth" : ":geral");
        long agora = System.currentTimeMillis();

        WindowEntry entrada = janelas.compute(chave, (k, v) -> {
            if (v == null || agora - v.inicioJanela() >= JANELA_MS) {
                return new WindowEntry(agora, new AtomicInteger(0));
            }
            return v;
        });

        int tentativa = entrada.contagem().incrementAndGet();
        if (tentativa > limite) {
            log.warn("Rate limit excedido para IP={} uri={} count={}", ip, req.getRequestURI(), tentativa);
            resp.setStatus(429);
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("""
                    {"code":"RATE_LIMIT_EXCEEDED","message":"Muitas requisições. Tente novamente em breve.","status":429}
                    """.strip());
            return;
        }

        chain.doFilter(req, resp);
    }

    private String obterIp(HttpServletRequest req) {
        String forwarded = req.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return req.getRemoteAddr();
    }
}
