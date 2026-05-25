package com.ford.specpulse.soap;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Interceptor CXF — valida JWT no header SOAP.
 *
 * O cliente SOAP deve incluir o token no envelope:
 *
 * <soapenv:Header>
 *   <auth:Authorization xmlns:auth="http://security.specpulse.ford.com/">
 *     Bearer eyJhbGci...
 *   </auth:Authorization>
 * </soapenv:Header>
 *
 * Endpoints publicos (listarMarcas, buscarVersaoPorSlug) nao requerem token.
 * Endpoints sensiveis (consultarFichaTecnica, criarComparacao) requerem perfil minimo ANALISTA.
 */
@Component
public class JwtSoapInterceptor extends AbstractSoapInterceptor {

    private static final Logger log = LoggerFactory.getLogger(JwtSoapInterceptor.class);

    private static final String NS_AUTH     = "http://security.specpulse.ford.com/";
    private static final String ELEM_AUTH   = "Authorization";
    private static final String HMAC_ALGO   = "HmacSHA256";

    private static final List<String> OPS_PROTEGIDAS = List.of(
            "consultarFichaTecnica",
            "criarComparacao"
    );

    @Value("${specpulse.jwt.segredo:dev-segredo-minimo-32-caracteres-aqui}")
    private String jwtSegredo;

    @Value("${specpulse.soap.auth-habilitada:false}")
    private boolean authHabilitada;

    public JwtSoapInterceptor() {
        super(Phase.PRE_INVOKE);
    }

    @Override
    public void handleMessage(SoapMessage message) throws Fault {
        if (!authHabilitada) return;

        String operacao = extrairOperacao(message);
        if (operacao == null || !OPS_PROTEGIDAS.contains(operacao)) return;

        String token = extrairTokenDoHeader(message);
        if (token == null) {
            log.warn("[SOAP-SECURITY] Token ausente para operacao protegida: {}", operacao);
            throw new Fault(new SecurityException(
                    "Autenticacao necessaria. Inclua Bearer token no SOAP Header."));
        }

        if (!validarJwt(token)) {
            log.warn("[SOAP-SECURITY] Token invalido para operacao: {}", operacao);
            throw new Fault(new SecurityException("Token JWT invalido ou expirado."));
        }

        log.info("[SOAP-SECURITY] Acesso autorizado para operacao: {}", operacao);
    }

    private String extrairTokenDoHeader(SoapMessage message) {
        try {
            org.apache.cxf.headers.Header header =
                    message.getHeader(new javax.xml.namespace.QName(NS_AUTH, ELEM_AUTH));
            if (header == null) return null;

            Object content = header.getObject();
            if (content instanceof org.w3c.dom.Element elem) {
                String texto = elem.getTextContent().trim();
                if (texto.startsWith("Bearer ")) return texto.substring(7);
                return texto;
            }
        } catch (Exception e) {
            log.debug("[SOAP-SECURITY] Erro ao extrair token do header: {}", e.getMessage());
        }
        return null;
    }

    private String extrairOperacao(SoapMessage message) {
        try {
            org.apache.cxf.service.model.OperationInfo op =
                    message.get(org.apache.cxf.service.model.OperationInfo.class);
            return op != null ? op.getName().getLocalPart() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Validacao basica de JWT HS256.
     * O projeto usa spring-security oauth2-resource-server para REST —
     * reutilizamos a mesma logica de validacao HMAC aqui para SOAP.
     */
    private boolean validarJwt(String token) {
        try {
            String[] partes = token.split("\\.");
            if (partes.length != 3) return false;

            String dadosAssinatura = partes[0] + "." + partes[1];
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(jwtSegredo.getBytes(StandardCharsets.UTF_8), HMAC_ALGO));
            byte[] assinatura = mac.doFinal(dadosAssinatura.getBytes(StandardCharsets.UTF_8));

            String assinaturaBase64 = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(assinatura);

            if (!assinaturaBase64.equals(partes[2])) return false;

            String payload = new String(Base64.getUrlDecoder().decode(partes[1]),
                    StandardCharsets.UTF_8);
            if (payload.contains("\"exp\":")) {
                long exp = Long.parseLong(
                        payload.replaceAll(".*\"exp\":(\\d+).*", "$1"));
                if (exp < System.currentTimeMillis() / 1000) return false;
            }

            return true;
        } catch (Exception e) {
            log.debug("[SOAP-SECURITY] Erro ao validar JWT: {}", e.getMessage());
            return false;
        }
    }
}