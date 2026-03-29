package com.cebraspe.record.response;

public record QuestaoLeiResponse(
        Long id,
        String enunciado,
        Boolean gabarito, // null antes de responder
        String comentario, // null antes de responder
        String resumoEstudo, // null antes de responder
        String tipoPegadinha, // null antes de responder
        String artigoRef,
        String nivel,
        Integer anoProva,
        String orgaoProva,
        Integer recorrencia,
        Boolean respondida,
        Boolean acertou) {
}