package com.cebraspe.record;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;

@Table("questao_respondida")
public record QuestaoRespondida(
        @Id Long id,
        Long questaoId,
        LocalDateTime respondidaEm) {
}