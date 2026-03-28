package com.cebraspe.record.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SalvarQuestaoRequest(
                Long cargoId,
                Long temaId,
                @NotBlank String grupo,
                @NotNull Long subtemaId,
                @NotBlank String enunciado,
                @NotNull Boolean gabarito,
                @NotBlank String explicacao,
                String tipoPegadinha,
                String artigoRef,
                @NotBlank String nivelDificuldade) {
}