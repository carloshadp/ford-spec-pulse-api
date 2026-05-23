package com.ford.specpulse.autenticacao.dominio;

import com.ford.specpulse.autenticacao.persistencia.UsuarioRepositorio;
import com.ford.specpulse.seguranca.Perfil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * Popula a tabela de usuarios na primeira execucao com cinco contas
 * representativas, uma para cada perfil. Idempotente: so insere se a tabela
 * estiver vazia. Substitui o que era feito por PropriedadesUsuarios +
 * InMemoryUserDetailsManager.
 */
@Component
public class SeedUsuariosRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedUsuariosRunner.class);

    private static final List<SeedUsuario> SEMENTES = List.of(
            new SeedUsuario("Leitor Padrao", "leitor@ford.internal", "leitor123", Perfil.SOMENTE_LEITURA),
            new SeedUsuario("Analista Padrao", "analista@ford.internal", "analista123", Perfil.ANALISTA),
            new SeedUsuario("Gerente Padrao", "gerente@ford.internal", "gerente123", Perfil.GERENTE),
            new SeedUsuario("Validador Padrao", "validador@ford.internal", "validador123", Perfil.VALIDADOR_DADOS),
            new SeedUsuario("Administrador Padrao", "admin@ford.internal", "admin123", Perfil.ADMINISTRADOR)
    );

    private final UsuarioRepositorio repositorio;
    private final PasswordEncoder codificador;

    public SeedUsuariosRunner(UsuarioRepositorio repositorio, PasswordEncoder codificador) {
        this.repositorio = repositorio;
        this.codificador = codificador;
    }


    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (repositorio.count() > 0) {
            log.info("Seed de usuarios ignorado: tabela ja contem {} registros.", repositorio.count());
            return;
        }
        log.info("Populando tabela usuarios com {} contas padrao.", SEMENTES.size());
        for (SeedUsuario s : SEMENTES) {
            Usuario usuario = new Usuario(s.nome(), s.email(),
                    codificador.encode(s.senha()), s.perfil());
            repositorio.save(usuario);
            log.info(" - {} ({})", s.email(), s.perfil());
        }
    }


    private record SeedUsuario(String nome, String email, String senha, Perfil perfil) {
    }
}
