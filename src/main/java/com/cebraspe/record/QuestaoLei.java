package com.cebraspe.record;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;

@Table("questao_lei")
public record QuestaoLei(
        @Id Long id,
        Long topicoId,
        Long artigoId,
        String enunciado,
        Boolean gabarito,
        String comentario,
        String resumoEstudo,
        String tipoPegadinha,
        String artigoRef,
        String nivel,
        Integer anoProva,
        String orgaoProva,
        Integer recorrencia,
        Boolean ativa,
        LocalDateTime createdAt) {
}