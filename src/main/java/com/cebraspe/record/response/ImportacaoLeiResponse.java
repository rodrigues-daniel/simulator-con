package com.cebraspe.record.response;

import java.util.List;

public record ImportacaoLeiResponse(
        int total,
        int materiasNovas,
        int topicosNovos,
        int artigosNovos,
        int questoesImportadas,
        int ignoradas,
        List<String> erros) {
}