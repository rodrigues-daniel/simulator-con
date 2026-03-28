package com.cebraspe.record.response;

public record QuestaoProvaResponse(
        Long id,
        Integer numero,
        String tema,
        String subtema,
        String enunciado,
        String nivelDificuldade,
        // Preenchidos apenas após responder:
        Boolean gabarito,
        String explicacao,
        String tipoPegadinha,
        String artigoRef,
        Boolean acertou,
        Long tempoMs) {
}