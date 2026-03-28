package com.cebraspe.record.dto;

public record EstatisticaSubtemaDto(
        Long subtemaId,
        Long total,
        Long acertos,
        Double tempoMedioMs) {
}