package com.ford.specpulse.veiculo.persistencia;

import com.ford.specpulse.veiculo.dominio.Mercado;
import com.ford.specpulse.veiculo.dominio.Segmento;
import com.ford.specpulse.veiculo.dominio.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VeiculoRepositorio extends JpaRepository<Veiculo, UUID> {

    @Query("SELECT v FROM Veiculo v JOIN FETCH v.marca ORDER BY v.marca.nome, v.modelo")
    List<Veiculo> findAll();

    @Query("SELECT v FROM Veiculo v JOIN FETCH v.marca WHERE v.id = :id")
    Optional<Veiculo> findById(UUID id);

    @Query("SELECT v FROM Veiculo v JOIN FETCH v.marca WHERE v.segmento = :segmento")
    List<Veiculo> findBySegmento(Segmento segmento);

    @Query("SELECT v FROM Veiculo v JOIN FETCH v.marca WHERE v.mercado = :mercado")
    List<Veiculo> findByMercado(Mercado mercado);

    @Query("SELECT v FROM Veiculo v JOIN FETCH v.marca WHERE v.marca.id = :marcaId")
    List<Veiculo> findByMarcaId(UUID marcaId);

    List<Veiculo> findByMarcaNomeIgnoreCaseAndModeloIgnoreCase(String marcaNome, String modelo);

    boolean existsByMarcaIdAndModeloIgnoreCaseAndAnoModeloAndMercado(
            UUID marcaId, String modelo, Integer anoModelo, Mercado mercado);
}
