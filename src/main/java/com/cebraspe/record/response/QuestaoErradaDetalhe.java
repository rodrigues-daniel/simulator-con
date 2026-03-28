package com.cebraspe.record.response;

public record QuestaoErradaDetalhe(
        Long questaoId,
        String tema,
        String subtema,
        String enunciado,
        Boolean gabarito,
        String explicacao,
        String tipoPegadinha,
        String artigoRef,
        String nivelDificuldade,
        long vezesErrada,
        double taxaAcerto,
        double tempoMedioMs) {
}