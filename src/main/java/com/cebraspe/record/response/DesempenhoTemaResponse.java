package com.cebraspe.record.response;

public record DesempenhoTemaResponse(
        Long temaId,
        String tema,
        long total,
        long acertos,
        double aproveitamento,
        double tempoMedioMs) {
}