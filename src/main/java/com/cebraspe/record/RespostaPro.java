package com.cebraspe.record;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;

@Table("resposta_pro")
public record RespostaPro(
        @Id Long id,
        Long sessaoId,
        Long questaoId,
        Long temaId,
        Long subtemaId,
        String nivel,
        Boolean resposta,
        Boolean acertou,
        Long tempoMs,
        LocalDateTime createdAt) {
}