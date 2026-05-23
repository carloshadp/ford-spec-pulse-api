package com.ford.specpulse.versao.dominio;

import com.ford.specpulse.compartilhado.RecursoNaoEncontradoException;
import com.ford.specpulse.veiculo.dominio.Veiculo;
import com.ford.specpulse.veiculo.dominio.VeiculoServico;
import com.ford.specpulse.versao.persistencia.VersaoRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;


@Service
public class VersaoServico {

    private final VersaoRepositorio repositorio;
    private final VeiculoServico veiculoServico;

    public VersaoServico(VersaoRepositorio repositorio, VeiculoServico veiculoServico) {
        this.repositorio = repositorio;
        this.veiculoServico = veiculoServico;
    }

    @Transactional(readOnly = true)
    public List<Versao> listarTodas() {
        return repositorio.findAll();
    }

    @Transactional(readOnly = true)
    public List<Versao> listarPorVeiculo(UUID veiculoId) {
        veiculoServico.buscarPorId(veiculoId);
        return repositorio.findByVeiculoId(veiculoId);
    }

    @Transactional(readOnly = true)
    public Versao buscarPorId(UUID id) {
        return repositorio.findById(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.porId("Versao", id));
    }

    @Transactional
    public Versao criar(UUID veiculoId, String nome, Powertrain powertrain,
                        String tracao, String cabine, BigDecimal precoSugerido) {
        Veiculo veiculo = veiculoServico.buscarPorId(veiculoId);
        return repositorio.save(new Versao(veiculo, nome, powertrain, tracao, cabine, precoSugerido));
    }
}
