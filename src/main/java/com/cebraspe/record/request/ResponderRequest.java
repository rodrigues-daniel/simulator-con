package com.cebraspe.record.request;

import jakarta.validation.constraints.NotNull;

public record ResponderRequest(
        @NotNull Long questaoId,
        @NotNull Boolean resposta,
        @NotNull Long tempoMs) {
}