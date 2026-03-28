package com.cebraspe.record.response;

public record RespostaProResponse(
        Long questaoId,
        Boolean acertou,
        Boolean gabarito,
        String explicacao,
        String tipoPegadinha,
        String artigoRef) {
}