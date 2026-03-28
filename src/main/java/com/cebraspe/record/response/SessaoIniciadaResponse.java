package com.cebraspe.record.response;

import java.util.List;

public record SessaoIniciadaResponse(
        Long sessaoId,
        Integer totalQuestoes,
        List<QuestaoProvaResponse> questoes) {
}