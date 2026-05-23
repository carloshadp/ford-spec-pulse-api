package com.ford.specpulse.config;

import com.ford.specpulse.auditoria.dominio.AuditoriaServico;
import com.ford.specpulse.autenticacao.dominio.PropriedadesJwt;
import com.ford.specpulse.autenticacao.dominio.TokenServico;
import com.ford.specpulse.compartilhado.RequestIdFilter;
import com.ford.specpulse.seguranca.Perfil;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


@EnableAsync
@Configuration
@EnableConfigurationProperties(PropriedadesJwt.class)
public class SecurityConfig {

    @Value("${specpulse.cors.origens:http://localhost:3000,http://localhost:8081,http://localhost:19006}")
    private String origensPermitidas;

    @Bean
    public PasswordEncoder codificadorSenha() {
        return new BCryptPasswordEncoder(10);
    }


    @Bean
    public JwtDecoder decodificadorJwt(PropriedadesJwt propriedades) {
        SecretKeySpec chave = new SecretKeySpec(
                propriedades.segredo().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(chave)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }


    @Bean
    public JwtEncoder codificadorJwt(PropriedadesJwt propriedades) {
        SecretKeySpec chave = new SecretKeySpec(
                propriedades.segredo().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        ImmutableSecret<SecurityContext> fonte = new ImmutableSecret<>(chave);
        return new NimbusJwtEncoder(fonte);
    }


    @Bean
    public JwtAuthenticationConverter conversorAutenticacaoJwt() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName(TokenServico.CLAIM_PERFIL);
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter conversor = new JwtAuthenticationConverter();
        conversor.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return conversor;
    }


    @Bean
    public CorsConfigurationSource configuracaoCors() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(origensPermitidas.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("X-Request-Id", "Location"));
        config.setAllowCredentials(false);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }


    @Bean
    public SecurityFilterChain cadeiaFiltrosSeguranca(HttpSecurity http,
                                                       JwtAuthenticationConverter conversor,
                                                       AuditoriaServico auditoriaServico) throws Exception {

        AuthenticationEntryPoint entryPoint = (req, resp, ex) -> {
            if (resp.isCommitted()) return;
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setContentType("application/json;charset=UTF-8");
            String requestId = MDC.get(RequestIdFilter.MDC_CHAVE);
            if (requestId == null) requestId = "n/a";
            String body = "{\"timestamp\":\"" + OffsetDateTime.now()
                    + "\",\"status\":401,\"code\":\"UNAUTHORIZED\",\"message\":"
                    + "\"Token ausente ou invalido. Faca login em POST /api/auth/login.\""
                    + ",\"path\":\"" + req.getRequestURI()
                    + "\",\"requestId\":\"" + requestId + "\",\"details\":[]}";
            resp.setContentLength(body.getBytes(StandardCharsets.UTF_8).length);
            try (var out = resp.getWriter()) {
                out.write(body);
                out.flush();
            }
        };

        AccessDeniedHandler accessDeniedHandler = (req, resp, ex) -> {
            if (resp.isCommitted()) return;
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.setContentType("application/json;charset=UTF-8");
            String requestId = MDC.get(RequestIdFilter.MDC_CHAVE);
            if (requestId == null) requestId = "n/a";
            String body = "{\"timestamp\":\"" + OffsetDateTime.now()
                    + "\",\"status\":403,\"code\":\"FORBIDDEN\",\"message\":"
                    + "\"Seu perfil nao possui permissao para esta operacao.\""
                    + ",\"path\":\"" + req.getRequestURI()
                    + "\",\"requestId\":\"" + requestId + "\",\"details\":[]}";
            resp.setContentLength(body.getBytes(StandardCharsets.UTF_8).length);
            try {
                var auth = SecurityContextHolder.getContext().getAuthentication();
                UUID userId = null;
                if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
                    userId = UUID.fromString(jwt.getSubject());
                }
                auditoriaServico.registrarErroAcesso(userId, req.getRequestURI(), req.getRemoteAddr());
            } catch (Exception ignored) {}
            try (var out = resp.getWriter()) {
                out.write(body);
                out.flush();
            }
        };

        http
                .cors(cors -> cors.configurationSource(configuracaoCors()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        // Documentação pública
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()

                        // Endpoints públicos de autenticação
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()

                        // Ficha técnica: leitura para todos os perfis autenticados
                        .requestMatchers(HttpMethod.POST, "/api/fichas-tecnicas/consultar").hasAnyRole(
                                Perfil.SOMENTE_LEITURA.name(), Perfil.ANALISTA.name(),
                                Perfil.GERENTE.name(), Perfil.VALIDADOR_DADOS.name(), Perfil.ADMINISTRADOR.name())

                        // Admin: gerenciamento de usuários
                        .requestMatchers(HttpMethod.GET, "/api/usuarios").hasRole(Perfil.ADMINISTRADOR.name())
                        .requestMatchers(HttpMethod.PATCH, "/api/usuarios/**").hasRole(Perfil.ADMINISTRADOR.name())

                        // Admin: audit log e operações administrativas
                        .requestMatchers(HttpMethod.GET, "/api/admin/**").hasRole(Perfil.ADMINISTRADOR.name())

                        // Data validator + admin: data quality e uploads
                        .requestMatchers("/api/qualidade-dados/**", "/api/uploads/**").hasAnyRole(
                                Perfil.VALIDADOR_DADOS.name(), Perfil.ADMINISTRADOR.name())

                        // Criação de comparações: analista, gerente, admin
                        .requestMatchers(HttpMethod.POST, "/api/comparacoes").hasAnyRole(
                                Perfil.ANALISTA.name(), Perfil.GERENTE.name(), Perfil.ADMINISTRADOR.name())

                        // Leituras gerais: todos os perfis autenticados
                        .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole(
                                Perfil.SOMENTE_LEITURA.name(), Perfil.ANALISTA.name(),
                                Perfil.GERENTE.name(), Perfil.VALIDADOR_DADOS.name(), Perfil.ADMINISTRADOR.name())

                        // Demais mutações: analista, gerente, admin
                        .requestMatchers("/api/**").hasAnyRole(
                                Perfil.ANALISTA.name(), Perfil.GERENTE.name(), Perfil.ADMINISTRADOR.name())

                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(conversor))
                        .authenticationEntryPoint(entryPoint));
        return http.build();
    }
}
