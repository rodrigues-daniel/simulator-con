package com.cebraspe.record;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;

@Table("progresso_topico")
public record ProgressoTopico(
        @Id Long id,
        Long topicoId,
        String status,
        Integer acertos,
        Integer erros,
        LocalDateTime ultimaRevisao,
        LocalDateTime updatedAt) {
}