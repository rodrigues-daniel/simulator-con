package com.cebraspe.record.response;

import java.util.List;

public record MateriaResponse(
        Long id,
        String nome,
        String descricao,
        String fonteLegal,
        int totalTopicos,
        int topicosConcluidosCount,
        double progressoPct,
        List<TopicoResumoResponse> topicos) {
}