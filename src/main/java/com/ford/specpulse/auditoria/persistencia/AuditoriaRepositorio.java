package com.ford.specpulse.auditoria.persistencia;

import com.ford.specpulse.auditoria.dominio.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuditoriaRepositorio extends JpaRepository<Auditoria, UUID> {
}
