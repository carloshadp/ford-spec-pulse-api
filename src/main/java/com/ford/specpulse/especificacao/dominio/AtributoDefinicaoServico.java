package com.ford.specpulse.especificacao.dominio;

import com.ford.specpulse.compartilhado.RecursoNaoEncontradoException;
import com.ford.specpulse.especificacao.persistencia.AtributoDefinicaoRepositorio;
import com.ford.specpulse.especificacao.persistencia.AtributoSinonimoRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AtributoDefinicaoServico {

    private final AtributoDefinicaoRepositorio repositorio;
    private final AtributoSinonimoRepositorio sinonimoRepositorio;

    public AtributoDefinicaoServico(AtributoDefinicaoRepositorio repositorio,
                                    AtributoSinonimoRepositorio sinonimoRepositorio) {
        this.repositorio = repositorio;
        this.sinonimoRepositorio = sinonimoRepositorio;
    }

    @Transactional(readOnly = true)
    public List<AtributoDefinicao> listarTodos() {
        return repositorio.findAll();
    }

    @Transactional(readOnly = true)
    public List<AtributoDefinicao> listarPorCategoria(Categoria categoria) {
        return repositorio.findByCategoria(categoria);
    }

    @Transactional(readOnly = true)
    public AtributoDefinicao buscarPorId(UUID id) {
        return repositorio.findById(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.porId("AtributoDefinicao", id));
    }

    @Transactional(readOnly = true)
    public Map<UUID, List<String>> sinonimosPorAtributo() {
        return sinonimoRepositorio.findAll().stream()
                .collect(Collectors.groupingBy(
                        s -> s.getAtributo().getId(),
                        Collectors.mapping(AtributoSinonimo::getTermo, Collectors.toList())));
    }

    /**
     * Resolve um termo livre (codigo canonico, nome de exibicao ou sinonimo) em um atributo.
     * Necessario para que o usuario possa pedir specs por "potencia", "cv", "cavalos" etc.
     */
    @Transactional(readOnly = true)
    public Optional<AtributoDefinicao> buscarPorTermo(String termo) {
        if (termo == null || termo.isBlank()) {
            return Optional.empty();
        }
        String normalizado = termo.trim();

        Optional<AtributoDefinicao> porCodigo = repositorio.findByCodigoCanonicoIgnoreCase(normalizado);
        if (porCodigo.isPresent()) {
            return porCodigo;
        }
        Optional<AtributoDefinicao> porNome = repositorio.findByNomeExibicaoIgnoreCase(normalizado);
        if (porNome.isPresent()) {
            return porNome;
        }
        return sinonimoRepositorio.findByTermoIgnoreCase(normalizado)
                .map(AtributoSinonimo::getAtributo);
    }
}
