package com.ford.specpulse.compartilhado;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.List;


@RestControllerAdvice
public class ManipuladorGlobalExcecoes {

    private static final Logger log = LoggerFactory.getLogger(ManipuladorGlobalExcecoes.class);

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<RespostaErro> tratarRecursoNaoEncontrado(
            RecursoNaoEncontradoException ex, HttpServletRequest req) {
        return montar(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), req, List.of());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<RespostaErro> tratarRegraNegocio(
            RegraNegocioException ex, HttpServletRequest req) {
        return montar(HttpStatus.UNPROCESSABLE_ENTITY, "BUSINESS_RULE_VIOLATION", ex.getMessage(), req, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespostaErro> tratarValidacao(
            MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<RespostaErro.FieldError> detalhes = ex.getBindingResult().getFieldErrors().stream()
                .map(this::converterErroCampo)
                .toList();
        return montar(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                "Um ou mais campos da requisição são inválidos.", req, detalhes);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<RespostaErro> tratarAcessoNegado(
            AccessDeniedException ex, HttpServletRequest req) {
        return montar(HttpStatus.FORBIDDEN, "FORBIDDEN",
                "Seu perfil não possui permissão para esta operação.", req, List.of());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<RespostaErro> tratarNaoAutenticado(
            AuthenticationException ex, HttpServletRequest req) {
        return montar(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED",
                "Credenciais ausentes ou inválidas.", req, List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespostaErro> tratarGenerico(Exception ex, HttpServletRequest req) {
        log.error("Erro não tratado em {}: {}", req.getRequestURI(), ex.getMessage(), ex);
        return montar(HttpStatus.INTERNAL_SERVER_ERROR, "SERVICE_UNAVAILABLE",
                "Ocorreu um erro inesperado. Consulte os logs para mais detalhes.", req, List.of());
    }

    private RespostaErro.FieldError converterErroCampo(FieldError erro) {
        return new RespostaErro.FieldError(erro.getField(), erro.getDefaultMessage());
    }

    private ResponseEntity<RespostaErro> montar(HttpStatus status, String code,
                                                String message, HttpServletRequest req,
                                                List<RespostaErro.FieldError> details) {
        String requestId = MDC.get(RequestIdFilter.MDC_CHAVE);
        RespostaErro corpo = new RespostaErro(
                OffsetDateTime.now(),
                status.value(),
                code,
                message,
                req.getRequestURI(),
                requestId,
                details
        );
        return ResponseEntity.status(status).body(corpo);
    }
}
