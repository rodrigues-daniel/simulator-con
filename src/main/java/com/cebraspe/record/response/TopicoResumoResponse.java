package com.cebraspe.record.response;

public record TopicoResumoResponse(
        Long id,
        String nome,
        String artigoBase,
        String status,
        int totalQuestoes,
        int acertos,
        int erros,
        double aproveitamento) {
}