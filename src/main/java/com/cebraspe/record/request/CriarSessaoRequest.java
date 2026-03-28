package com.cebraspe.record.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CriarSessaoRequest(
        Long cargoId,
        List<Long> temaIds,
        List<Long> subtemaIds,
        @Min(5) @NotNull Integer totalQuestoes,
        Integer pctFacil, // % de fáceis (0-100)
        Integer pctMedio, // % de médias
        Integer pctDificil, // % de difíceis
        Boolean aleatorio, // true = ignora % e mistura tudo
        Boolean focarDificuldades, // true = prioriza subtemas com < 60% acerto
        @NotBlank String grupo, // NOVO: "EDITAL" | "BANCA"
        String modoCard // NOVO: "lista" | "um_por_vez"

) {
}