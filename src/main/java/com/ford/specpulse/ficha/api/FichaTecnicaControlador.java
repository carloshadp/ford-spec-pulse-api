package com.ford.specpulse.ficha.api;

import com.ford.specpulse.ficha.api.dto.ConsultaFichaTecnicaRequisicao;
import com.ford.specpulse.ficha.api.dto.FichaTecnicaResposta;
import com.ford.specpulse.ficha.dominio.FichaTecnicaServico;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Tag(name = "Fichas Tecnicas",
        description = "Consulta de ficha tecnica padronizada (atende ao Desafio 01 - Inteligencia Competitiva Automotiva).")
@RestController
@RequestMapping("/api/fichas-tecnicas")
public class FichaTecnicaControlador {

    private final FichaTecnicaServico servico;

    public FichaTecnicaControlador(FichaTecnicaServico servico) {
        this.servico = servico;
    }

    @Operation(summary = "Consulta a ficha tecnica de uma versao por marca/modelo/versao com lista livre de atributos.",
            description = "Resolve o veiculo por nome de marca + modelo + versao (case-insensitive, com matching parcial " +
                    "na versao). Para cada atributo solicitado, tenta casar com codigo canonico, nome de exibicao ou " +
                    "sinonimo cadastrado. A resposta sempre tem um item por atributo solicitado, com status " +
                    "PRESENTE / NAO_INFORMADO / NAO_DISPONIVEL / ATRIBUTO_DESCONHECIDO indicando como interpretar o item.")
    @PostMapping("/consultar")
    public FichaTecnicaResposta consultar(@Valid @RequestBody ConsultaFichaTecnicaRequisicao requisicao) {
        return servico.consultar(requisicao);
    }
}
