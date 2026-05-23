package com.ford.specpulse.auditoria.dominio;

import com.ford.specpulse.auditoria.persistencia.AuditoriaRepositorio;
import com.ford.specpulse.compartilhado.RequestIdFilter;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuditoriaServico {

    private final AuditoriaRepositorio repositorio;

    public AuditoriaServico(AuditoriaRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(String acao, UUID usuarioId, String entidade,
                          String entidadeId, String dadosJson, String ip) {
        String requestId = MDC.get(RequestIdFilter.MDC_CHAVE);
        Auditoria evento = new Auditoria(requestId, usuarioId, acao, entidade, entidadeId, dadosJson, ip);
        repositorio.save(evento);
    }

    public void registrarLogin(String email, String ip, boolean sucesso) {
        String acao = sucesso ? "LOGIN_SUCESSO" : "LOGIN_FALHA";
        String dados = "{\"email\":\"" + email + "\",\"sucesso\":" + sucesso + "}";
        registrar(acao, null, "usuario", email, dados, ip);
    }

    public void registrarRegistro(String email, String ip) {
        String dados = "{\"email\":\"" + email + "\"}";
        registrar("REGISTRO_USUARIO", null, "usuario", email, dados, ip);
    }

    public void registrarCriacaoComparacao(UUID usuarioId, String comparacaoId, String ip) {
        registrar("CRIACAO_COMPARACAO", usuarioId, "comparacao", comparacaoId, null, ip);
    }

    public void registrarErroAcesso(UUID usuarioId, String caminho, String ip) {
        String dados = "{\"caminho\":\"" + caminho + "\"}";
        registrar("ACESSO_NEGADO", usuarioId, "endpoint", caminho, dados, ip);
    }

    public void registrarAtualizacaoUsuario(UUID adminId, String alvoId, String mudancas, String ip) {
        String dados = "{\"mudancas\":\"" + mudancas + "\"}";
        registrar("ATUALIZACAO_USUARIO", adminId, "usuario", alvoId, dados, ip);
    }
}
