package com.ford.specpulse.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * Politica de retencao e descarte seguro de dados.
 *
 * Executa jobs agendados para:
 * 1. Remover refresh tokens expirados ou revogados
 * 2. Anonimizar registros de auditoria com mais de 90 dias
 * 3. Remover tokens expirados de acesso
 *
 * Executa diariamente as 02:00 para minimizar impacto em producao.
 * Logs sem dados sensiveis — apenas contagens de registros afetados.
 */
@Service
public class DataRetentionService {

    private static final Logger log = LoggerFactory.getLogger(DataRetentionService.class);

    private static final int DIAS_RETENCAO_AUDIT   = 90;
    private static final int DIAS_RETENCAO_TOKENS  = 7;

    private final JdbcTemplate jdbc;

    public DataRetentionService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Remove refresh tokens expirados e revogados.
     * Executa diariamente as 02:00.
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void removerTokensExpirados() {
        log.info("[RETENCAO] Iniciando limpeza de refresh tokens expirados");

        int removidos = jdbc.update("""
            DELETE FROM refresh_tokens
            WHERE revogado_em IS NOT NULL
               OR expira_em < ?
            """, OffsetDateTime.now().minusDays(DIAS_RETENCAO_TOKENS));

        log.info("[RETENCAO] Refresh tokens removidos: {}", removidos);
    }

    /**
     * Anonimiza registros de auditoria com mais de 90 dias.
     * Substitui IP e usuario_id por valores anonimizados.
     * Executa diariamente as 02:30.
     */
    @Scheduled(cron = "0 30 2 * * *")
    @Transactional
    public void anonimizarAuditoriaAntiga() {
        log.info("[RETENCAO] Iniciando anonimizacao de registros de auditoria antigos");

        OffsetDateTime limite = OffsetDateTime.now().minusDays(DIAS_RETENCAO_AUDIT);

        int anonimizados = jdbc.update("""
            UPDATE auditoria
            SET ip          = 'ANONIMIZADO',
                usuario_id  = NULL,
                entidade_id = CASE
                    WHEN entidade_id LIKE '%@%' THEN 'ANONIMIZADO'
                    ELSE entidade_id
                END,
                dados_json  = NULL
            WHERE ocorrido_em < ?
              AND ip != 'ANONIMIZADO'
            """, limite);

        log.info("[RETENCAO] Registros de auditoria anonimizados: {}", anonimizados);
    }

    /**
     * Remove registros de auditoria com mais de 1 ano (apos anonimizacao).
     * Executa semanalmente aos domingos as 03:00.
     */
    @Scheduled(cron = "0 0 3 * * SUN")
    @Transactional
    public void removerAuditoriaAntiga() {
        log.info("[RETENCAO] Removendo registros de auditoria com mais de 1 ano");

        OffsetDateTime limite = OffsetDateTime.now().minusDays(365);

        int removidos = jdbc.update("""
            DELETE FROM auditoria
            WHERE ocorrido_em < ?
              AND ip = 'ANONIMIZADO'
            """, limite);

        log.info("[RETENCAO] Registros de auditoria removidos: {}", removidos);
    }
}