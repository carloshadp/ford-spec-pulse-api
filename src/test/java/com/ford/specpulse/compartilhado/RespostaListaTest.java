package com.ford.specpulse.compartilhado;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Paginacao com limite maximo de pageSize")
class RespostaListaTest {

    private final List<Integer> itens = List.of(1, 2, 3);

    @Test
    @DisplayName("pageSize acima do maximo e reduzido a 100, nao aceito como veio")
    void pageSizeExcessivoELimitado() {
        RespostaLista<Integer> resposta = RespostaLista.paginada(itens, 1, 100_000);

        assertThat(resposta.pageSize()).isEqualTo(100);
    }

    @Test
    @DisplayName("pageSize dentro do limite permanece igual")
    void pageSizeNormal() {
        RespostaLista<Integer> resposta = RespostaLista.paginada(itens, 1, 2);

        assertThat(resposta.pageSize()).isEqualTo(2);
        assertThat(resposta.data()).hasSize(2);
    }
}
