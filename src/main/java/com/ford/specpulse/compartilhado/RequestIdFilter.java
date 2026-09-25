package com.ford.specpulse.compartilhado;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Primeiro filtro da cadeia: o requestId precisa existir antes do Spring Security,
 * senao as respostas 401/403 e os logs de acesso negado saem sem ele.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {

    public static final String MDC_CHAVE = "requestId";
    private static final String CABECALHO = "X-Request-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws ServletException, IOException {
        String id = req.getHeader(CABECALHO);
        if (id == null || id.isBlank()) {
            id = "req_" + UUID.randomUUID().toString().replace("-", "");
        }
        MDC.put(MDC_CHAVE, id);
        resp.setHeader(CABECALHO, id);
        try {
            chain.doFilter(req, resp);
        } finally {
            MDC.remove(MDC_CHAVE);
        }
    }
}
