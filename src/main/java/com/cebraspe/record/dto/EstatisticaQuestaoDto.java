package com.cebraspe.record.dto;

public record EstatisticaQuestaoDto(
        Long questaoId,
        Long total,
        Long acertos,
        Double tempoMedioMs) {
}