package com.cebraspe.record.dto;

public record EstatisticaTemaDto(
        Long temaId,
        Long total,
        Long acertos,
        Double tempoMedioMs) {
}