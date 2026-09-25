package com.ford.specpulse.compartilhado;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

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

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RespostaErro> tratarCorpoIlegivel(
            HttpMessageNotReadableException ex, HttpServletRequest req) {
        return montar(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                "Corpo da requisição ausente ou com JSON inválido.", req, List.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<RespostaErro> tratarTipoInvalido(
            MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        return montar(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
                "Um ou mais parâmetros da requisição são inválidos.", req,
                List.of(new RespostaErro.FieldError(ex.getName(), "tipo de valor inválido")));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespostaErro> tratarGenerico(Exception ex, HttpServletRequest req) {
        if (ex instanceof ErrorResponse erroHttp) {
            return tratarErroHttp(erroHttp, req);
        }
        log.error("Erro não tratado em {}: {}", req.getRequestURI(), ex.getMessage(), ex);
        return montar(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Ocorreu um erro inesperado. Consulte os logs para mais detalhes.", req, List.of());
    }

    /**
     * Excecoes do Spring MVC que ja carregam o status HTTP correto (rota inexistente,
     * metodo nao suportado, Content-Type invalido, parametro obrigatorio ausente...).
     * Mantem o status e os headers (ex.: Allow no 405) e so padroniza o corpo.
     */
    private ResponseEntity<RespostaErro> tratarErroHttp(ErrorResponse erro, HttpServletRequest req) {
        HttpStatus status = HttpStatus.valueOf(erro.getStatusCode().value());
        String code = status == HttpStatus.BAD_REQUEST ? "VALIDATION_ERROR" : status.name();
        String message = switch (status) {
            case NOT_FOUND -> "Recurso não encontrado.";
            case METHOD_NOT_ALLOWED -> "Método HTTP não suportado para este recurso.";
            case UNSUPPORTED_MEDIA_TYPE -> "Content-Type não suportado. Use application/json.";
            default -> status.is5xxServerError()
                    ? "Serviço temporariamente indisponível."
                    : "Requisição inválida.";
        };
        RespostaErro corpo = montar(status, code, message, req, List.of()).getBody();
        return ResponseEntity.status(status).headers(erro.getHeaders()).body(corpo);
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
