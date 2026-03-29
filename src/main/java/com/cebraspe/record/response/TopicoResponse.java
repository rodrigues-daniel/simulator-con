package com.cebraspe.record.response;

import java.util.List;

public record TopicoResponse(
        Long id,
        String nome,
        String artigoBase,
        String resumo,
        String status,
        int acertos,
        int erros,
        List<ArtigoResponse> artigos,
        List<QuestaoLeiResponse> questoes) {
}