package com.cebraspe.record.response;

public record DesempenhoSubtemaResponse(
        Long subtemaId,
        String subtema,
        String tema,
        long total,
        long acertos,
        double aproveitamento,
        double tempoMedioMs) {
}