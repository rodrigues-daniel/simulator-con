package com.cebraspe.record.response;

import java.util.List;

public record ImportacaoProResponse(
        int total,
        int importadas,
        int ignoradas,
        int temasNovos,
        int subtemasNovos,
        List<String> erros) {
}