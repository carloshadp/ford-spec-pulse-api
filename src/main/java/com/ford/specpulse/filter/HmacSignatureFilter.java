package com.ford.specpulse.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Set;

/**
 * Filtro HMAC-SHA256 — verifica assinatura de integridade dos payloads.
 *
 * Endpoints protegidos esperam o header:
 *   X-Signature-256: sha256=<hex(HMAC-SHA256(body, segredo))>
 *
 * Clientes que nao suportam HMAC podem ser excluidos via
 * specpulse.hmac.excluir-paths (lista separada por virgula).
 *
 * Para gerar a assinatura no Postman (pre-request script):
 *   const body  = pm.request.body.raw;
 *   const secret = pm.environment.get("hmac_secret");
 *   const sig   = CryptoJS.HmacSHA256(body, secret).toString();
 *   pm.request.headers.add({ key: "X-Signature-256", value: "sha256=" + sig });
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
public class HmacSignatureFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(HmacSignatureFilter.class);
    private static final String HEADER = "X-Signature-256";
    private static final String ALGO   = "HmacSHA256";

    @Value("${specpulse.hmac.segredo:}")
    private String hmacSecret;

    @Value("${specpulse.hmac.habilitado:false}")
    private boolean hmacHabilitado;

    private static final Set<String> PATHS_PROTEGIDOS = Set.of(
            "/api/comparacoes",
            "/api/fichas-tecnicas/consultar"
    );

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  request  = (HttpServletRequest)  req;
        HttpServletResponse response = (HttpServletResponse) res;

        if (!hmacHabilitado || !deveVerificar(request.getRequestURI())) {
            chain.doFilter(req, res);
            return;
        }

        String header = request.getHeader(HEADER);
        if (header == null || !header.startsWith("sha256=")) {
            log.warn("HMAC ausente na requisicao para {}", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("""
                {"status":401,"code":"HMAC_AUSENTE",
                 "message":"Header X-Signature-256 obrigatorio para este endpoint."}
                """);
            return;
        }

        byte[] body = request.getInputStream().readAllBytes();
        String assinaturaRecebida = header.substring(7);
        String assinaturaEsperada = calcularHmac(body, hmacSecret);

        if (!assinaturaEsperada.equals(assinaturaRecebida)) {
            log.warn("Assinatura HMAC invalida para {}", request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("""
                {"status":401,"code":"HMAC_INVALIDO",
                 "message":"Assinatura de integridade do payload invalida."}
                """);
            return;
        }

        chain.doFilter(req, res);
    }

    private boolean deveVerificar(String uri) {
        return PATHS_PROTEGIDOS.stream().anyMatch(uri::startsWith);
    }

    public static String calcularHmac(byte[] dados, String segredo) {
        try {
            Mac mac = Mac.getInstance(ALGO);
            mac.init(new SecretKeySpec(segredo.getBytes(StandardCharsets.UTF_8), ALGO));
            return HexFormat.of().formatHex(mac.doFinal(dados));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Erro ao calcular HMAC", e);
        }
    }
}