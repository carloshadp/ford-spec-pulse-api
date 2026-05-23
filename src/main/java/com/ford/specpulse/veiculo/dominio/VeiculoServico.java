package com.ford.specpulse.veiculo.dominio;

import com.ford.specpulse.compartilhado.RecursoNaoEncontradoException;
import com.ford.specpulse.compartilhado.RegraNegocioException;
import com.ford.specpulse.veiculo.persistencia.VeiculoRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
public class VeiculoServico {

    private final VeiculoRepositorio repositorio;
    private final MarcaServico marcaServico;

    public VeiculoServico(VeiculoRepositorio repositorio, MarcaServico marcaServico) {
        this.repositorio = repositorio;
        this.marcaServico = marcaServico;
    }

    @Transactional(readOnly = true)
    public List<Veiculo> listarTodos() {
        return repositorio.findAll();
    }

    @Transactional(readOnly = true)
    public List<Veiculo> listarPorSegmento(Segmento segmento) {
        return repositorio.findBySegmento(segmento);
    }

    @Transactional(readOnly = true)
    public List<Veiculo> listarPorMarca(UUID marcaId) {
        return repositorio.findByMarcaId(marcaId);
    }

    @Transactional(readOnly = true)
    public Veiculo buscarPorId(UUID id) {
        return repositorio.findById(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.porId("Veiculo", id));
    }

    @Transactional
    public Veiculo criar(UUID marcaId, String modelo, Segmento segmento, Mercado mercado, Integer anoModelo) {
        Marca marca = marcaServico.buscarPorId(marcaId);
        boolean jaExiste = repositorio.existsByMarcaIdAndModeloIgnoreCaseAndAnoModeloAndMercado(
                marcaId, modelo, anoModelo, mercado);
        if (jaExiste) {
            throw new RegraNegocioException(
                    "Ja existe um veiculo cadastrado para esta marca/modelo/ano/mercado.");
        }
        return repositorio.save(new Veiculo(marca, modelo, segmento, mercado, anoModelo));
    }
}
