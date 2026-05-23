package com.ford.specpulse.versao.persistencia;

import com.ford.specpulse.versao.dominio.Versao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VersaoRepositorio extends JpaRepository<Versao, UUID> {

    @Query("SELECT v FROM Versao v JOIN FETCH v.veiculo vei JOIN FETCH vei.marca ORDER BY v.nome")
    List<Versao> findAll();

    @Query("SELECT v FROM Versao v JOIN FETCH v.veiculo vei JOIN FETCH vei.marca WHERE v.id = :id")
    Optional<Versao> findById(UUID id);

    @Query("SELECT v FROM Versao v JOIN FETCH v.veiculo vei JOIN FETCH vei.marca WHERE v.veiculo.id = :veiculoId ORDER BY v.nome")
    List<Versao> findByVeiculoId(UUID veiculoId);

    List<Versao> findByVeiculoIdAndNomeIgnoreCase(UUID veiculoId, String nome);

    List<Versao> findByVeiculoIdAndNomeContainingIgnoreCase(UUID veiculoId, String trecho);
}
