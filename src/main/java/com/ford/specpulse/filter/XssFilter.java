package com.ford.specpulse.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * Filtro XSS — sanitiza todos os parametros e corpo das requisicoes.
 * Remove tags HTML, scripts e sequencias de injecao antes de chegar
 * aos controllers, prevenindo XSS e HTML Injection.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class XssFilter implements Filter {

    private static final Pattern[] XSS_PATTERNS = {
            Pattern.compile("<script>(.*?)</script>",        Pattern.CASE_INSENSITIVE),
            Pattern.compile("<script[^>]*>",                 Pattern.CASE_INSENSITIVE),
            Pattern.compile("</script>",                     Pattern.CASE_INSENSITIVE),
            Pattern.compile("eval\\((.*?)\\)",               Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
            Pattern.compile("expression\\((.*?)\\)",         Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
            Pattern.compile("javascript:",                   Pattern.CASE_INSENSITIVE),
            Pattern.compile("vbscript:",                     Pattern.CASE_INSENSITIVE),
            Pattern.compile("onload(.*?)=",                  Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
            Pattern.compile("on\\w+(\\s*)=",                 Pattern.CASE_INSENSITIVE),
            Pattern.compile("<iframe(.*?)>",                 Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
            Pattern.compile("src[\r\n]*=[\r\n]*'(.*?)'",    Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
            Pattern.compile("src[\r\n]*=[\r\n]*\"(.*?)\"",  Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
            Pattern.compile("<(.*?)>",                       Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
    };

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        chain.doFilter(new XssRequestWrapper((HttpServletRequest) request), response);
    }

    public static String sanitize(String value) {
        if (value == null) return null;
        String result = value;
        for (Pattern pattern : XSS_PATTERNS) {
            result = pattern.matcher(result).replaceAll("");
        }
        result = result
                .replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
        return result.trim();
    }

    static class XssRequestWrapper extends HttpServletRequestWrapper {

        private byte[] sanitizedBody;

        XssRequestWrapper(HttpServletRequest request) throws IOException {
            super(request);
            String body = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            this.sanitizedBody = sanitize(body).getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public String[] getParameterValues(String name) {
            String[] values = super.getParameterValues(name);
            if (values == null) return null;
            String[] sanitized = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                sanitized[i] = sanitize(values[i]);
            }
            return sanitized;
        }

        @Override
        public String getParameter(String name) {
            return sanitize(super.getParameter(name));
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream bais = new ByteArrayInputStream(sanitizedBody);
            return new ServletInputStream() {
                public boolean isFinished() { return bais.available() == 0; }
                public boolean isReady()    { return true; }
                public void setReadListener(ReadListener l) {}
                public int read()           { return bais.read(); }
            };
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(
                    new ByteArrayInputStream(sanitizedBody), StandardCharsets.UTF_8));
        }
    }
}