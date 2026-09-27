package com.ford.specpulse.compartilhado;

import java.util.List;

public record RespostaLista<T>(
        List<T> data,
        int page,
        int pageSize,
        long total
) {
    public static <T> RespostaLista<T> completa(List<T> dados) {
        int tamanho = dados.size();
        return new RespostaLista<>(dados, 1, Math.max(tamanho, 1), tamanho);
    }

    private static final int TAMANHO_PAGINA_MAXIMO = 100;

    public static <T> RespostaLista<T> paginada(List<T> todos, int page, int pageSize) {
        int paginaReal = Math.max(1, page);
        int tamanhoReal = pageSize > 0 ? Math.min(pageSize, TAMANHO_PAGINA_MAXIMO) : 25;
        int inicio = (paginaReal - 1) * tamanhoReal;
        if (inicio >= todos.size()) {
            return new RespostaLista<>(List.of(), paginaReal, tamanhoReal, todos.size());
        }
        int fim = Math.min(inicio + tamanhoReal, todos.size());
        return new RespostaLista<>(todos.subList(inicio, fim), paginaReal, tamanhoReal, todos.size());
    }
}
