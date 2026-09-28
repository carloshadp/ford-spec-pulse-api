package com.ford.specpulse.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

/**
 * Rotina de backup e recuperacao do banco.
 *
 * Backup: exporta todo o schema e os dados via H2 SCRIPT TO, compactado em
 * .zip, para specpulse.backup.diretorio. Executado sob demanda
 * (POST /api/admin/backup, apenas admin) e agendado diariamente as 04:00.
 *
 * Recuperacao: RUNSCRIPT FROM le o arquivo gerado e recria o banco. E uma
 * operacao destrutiva (sobrescreve os dados atuais), por isso nao e exposta
 * via API — e um passo operacional (ver restaurar()), coberto por teste
 * automatizado (BackupServiceTest) para provar que o ciclo completo
 * funciona, mas disparado manualmente em produção.
 */
@Service
public class BackupService {

    private static final Logger log = LoggerFactory.getLogger(BackupService.class);
    private static final Pattern NOME_VALIDO = Pattern.compile("specpulse-\\d{8}-\\d{6}\\.zip");
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final JdbcTemplate jdbc;
    private final String diretorioBackup;

    public BackupService(JdbcTemplate jdbc,
                          @Value("${specpulse.backup.diretorio:./backups}") String diretorioBackup) {
        this.jdbc = jdbc;
        this.diretorioBackup = diretorioBackup;
    }

    public String executarBackup() {
        String nomeArquivo = "specpulse-" + OffsetDateTime.now().format(TIMESTAMP) + ".zip";
        Path destino = Path.of(diretorioBackup, nomeArquivo);
        criarDiretorio(destino.getParent());

        jdbc.execute("SCRIPT TO '" + destino.toAbsolutePath() + "' COMPRESSION ZIP");
        log.info("[BACKUP] Backup gerado: {}", nomeArquivo);
        return nomeArquivo;
    }

    /**
     * Restaura o banco a partir de um arquivo gerado por executarBackup().
     * Destrutivo: apaga o conteudo atual das tabelas antes de importar.
     */
    public void restaurar(String nomeArquivo) {
        if (nomeArquivo == null || !NOME_VALIDO.matcher(nomeArquivo).matches()) {
            throw new IllegalArgumentException("Nome de arquivo de backup invalido");
        }
        Path base = Path.of(diretorioBackup).toAbsolutePath().normalize();
        Path origem = base.resolve(nomeArquivo).normalize();
        if (!origem.startsWith(base)) {
            throw new IllegalArgumentException("Arquivo fora do diretorio de backup");
        }
        log.warn("[BACKUP] Iniciando restauracao a partir de {}", nomeArquivo);
        // RUNSCRIPT recria os objetos do zero; o schema atual precisa estar
        // vazio, senao os CREATE TABLE do script falham (objeto ja existe).
        jdbc.execute("DROP ALL OBJECTS");
        jdbc.execute("RUNSCRIPT FROM '" + origem + "' COMPRESSION ZIP");
        log.warn("[BACKUP] Restauracao concluida a partir de {}", nomeArquivo);
    }

    @Scheduled(cron = "0 0 4 * * *")
    public void backupAgendado() {
        executarBackup();
    }

    private void criarDiretorio(Path diretorio) {
        try {
            Files.createDirectories(diretorio);
        } catch (IOException e) {
            throw new IllegalStateException("Nao foi possivel criar o diretorio de backup: " + diretorio, e);
        }
    }
}
