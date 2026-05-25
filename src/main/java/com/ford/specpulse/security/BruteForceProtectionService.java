package com.ford.specpulse.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Protecao contra brute force — monitora tentativas de login invalidas.
 *
 * Regras:
 * - Mais de 5 tentativas falhas em 15 minutos bloqueia o IP por 30 minutos.
 * - Evento suspeito e registrado no log estruturado com nivel WARN.
 * - Apos desbloqueio automatico, contador e zerado.
 */
@Service
public class BruteForceProtectionService {

    private static final Logger log = LoggerFactory.getLogger(BruteForceProtectionService.class);

    private static final int  MAX_TENTATIVAS    = 5;
    private static final long JANELA_MS         = 15 * 60 * 1000L;
    private static final long BLOQUEIO_MS       = 30 * 60 * 1000L;

    record TentativaInfo(int contador, Instant primeiraFalha, Instant bloqueadoAte) {}

    private final Map<String, TentativaInfo> tentativas = new ConcurrentHashMap<>();

    public void registrarFalha(String ip, String email) {
        tentativas.compute(ip, (key, info) -> {
            Instant agora = Instant.now();

            if (info == null || expirou(info.primeiraFalha())) {
                log.warn("[SEGURANCA] Falha de login - ip={} email={} tentativa=1", ip, mascarar(email));
                return new TentativaInfo(1, agora, null);
            }

            int novoContador = info.contador() + 1;
            log.warn("[SEGURANCA] Falha de login - ip={} email={} tentativa={}", ip, mascarar(email), novoContador);

            if (novoContador >= MAX_TENTATIVAS) {
                Instant bloqueadoAte = agora.plusMillis(BLOQUEIO_MS);
                log.warn("[SEGURANCA] IP BLOQUEADO por brute force - ip={} bloqueado_ate={}", ip, bloqueadoAte);
                return new TentativaInfo(novoContador, info.primeiraFalha(), bloqueadoAte);
            }

            return new TentativaInfo(novoContador, info.primeiraFalha(), null);
        });
    }

    public boolean estaBloqueado(String ip) {
        TentativaInfo info = tentativas.get(ip);
        if (info == null || info.bloqueadoAte() == null) return false;

        if (Instant.now().isAfter(info.bloqueadoAte())) {
            tentativas.remove(ip);
            return false;
        }
        return true;
    }

    public void registrarSucesso(String ip) {
        tentativas.remove(ip);
    }

    private boolean expirou(Instant primeira) {
        return Instant.now().isAfter(primeira.plusMillis(JANELA_MS));
    }

    private String mascarar(String email) {
        if (email == null || !email.contains("@")) return "***";
        int at = email.indexOf('@');
        return email.charAt(0) + "***" + email.substring(at);
    }
}