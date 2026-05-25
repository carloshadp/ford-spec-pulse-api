package com.ford.specpulse.util;

import java.util.regex.Pattern;

/**
 * Utilitario para mascaramento de dados sensiveis em logs.
 *
 * Garante que emails, IPs, tokens e senhas nunca apareçam
 * em texto claro nos arquivos de log.
 *
 * Uso:
 *   log.info("Usuario autenticado: {}", LogMaskingUtils.email(email));
 *   log.warn("Acesso de: {}", LogMaskingUtils.ip(ipAddress));
 */
public final class LogMaskingUtils {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("([a-zA-Z0-9._%+\\-])([a-zA-Z0-9._%+\\-]*)(@.+)");

    private LogMaskingUtils() {}

    /**
     * Mascara email: j***@domain.com
     */
    public static String email(String email) {
        if (email == null) return "[null]";
        var matcher = EMAIL_PATTERN.matcher(email);
        if (matcher.matches()) {
            return matcher.group(1) + "***" + matcher.group(3);
        }
        return "***";
    }

    /**
     * Mascara IP: 192.168.*.*
     */
    public static String ip(String ip) {
        if (ip == null) return "[null]";
        String[] parts = ip.split("\\.");
        if (parts.length == 4) {
            return parts[0] + "." + parts[1] + ".*.*";
        }
        return ip.substring(0, Math.min(ip.length(), 4)) + "***";
    }

    /**
     * Mascara token JWT: exibe apenas os primeiros 8 chars.
     */
    public static String token(String token) {
        if (token == null) return "[null]";
        if (token.length() <= 8) return "***";
        return token.substring(0, 8) + "...";
    }

    /**
     * Substitui qualquer valor por ***.
     */
    public static String sensivel(Object valor) {
        return valor != null ? "***" : "[null]";
    }

    /**
     * Mascara UUID: exibe apenas os primeiros 8 chars.
     */
    public static String uuid(String id) {
        if (id == null) return "[null]";
        if (id.length() < 8) return "***";
        return id.substring(0, 8) + "...";
    }
}