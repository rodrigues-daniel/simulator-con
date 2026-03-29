package com.cebraspe.record.request;

import jakarta.validation.constraints.NotNull;

public record ResponderLeiRequest(
        @NotNull Long questaoId,
        @NotNull Boolean resposta) {
}