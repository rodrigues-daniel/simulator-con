package com.cebraspe.record.response;

public record DesempenhoNivelResponse(
        String nivel,
        long total,
        long acertos,
        double aproveitamento) {
}