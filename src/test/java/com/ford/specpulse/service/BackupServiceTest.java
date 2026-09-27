package com.ford.specpulse.service;

import com.ford.specpulse.suporte.TesteIntegracaoBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Backup e recuperacao do banco")
class BackupServiceTest extends TesteIntegracaoBase {

    @TempDir
    static Path diretorioBackup;

    @DynamicPropertySource
    static void diretorioBackupIsolado(DynamicPropertyRegistry registry) {
        registry.add("specpulse.backup.diretorio", () -> diretorioBackup.toString());
    }

    @Autowired
    private BackupService backupService;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    @DisplayName("admin aciona backup via API (201) e o arquivo e gerado de verdade em disco")
    void adminAcionaBackup() throws Exception {
        mvc.perform(post("/api/admin/backup").header("Authorization", bearer("admin")))
                .andExpect(status().isCreated());

        try (var arquivos = java.nio.file.Files.list(diretorioBackup)) {
            assertThat(arquivos.anyMatch(f -> f.toString().endsWith(".zip"))).isTrue();
        }
    }

    @Test
    @DisplayName("perfil nao-admin nao aciona backup (403)")
    void naoAdminNaoAcionaBackup() throws Exception {
        mvc.perform(post("/api/admin/backup").header("Authorization", bearer("gerente")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ciclo completo: backup -> dado apagado -> restauracao -> dado volta")
    void backupERestauracaoRoundTrip() {
        String email = "leitor@ford.internal";
        Integer antes = jdbc.queryForObject(
                "SELECT COUNT(*) FROM usuarios WHERE email = ?", Integer.class, email);
        assertThat(antes).isEqualTo(1);

        String arquivo = backupService.executarBackup();

        jdbc.update("DELETE FROM usuarios WHERE email = ?", email);
        Integer apagado = jdbc.queryForObject(
                "SELECT COUNT(*) FROM usuarios WHERE email = ?", Integer.class, email);
        assertThat(apagado).isEqualTo(0);

        backupService.restaurar(arquivo);

        Integer depois = jdbc.queryForObject(
                "SELECT COUNT(*) FROM usuarios WHERE email = ?", Integer.class, email);
        assertThat(depois).isEqualTo(1);
    }
}
