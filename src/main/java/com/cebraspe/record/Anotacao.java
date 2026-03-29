package com.cebraspe.record;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;

@Table("anotacao")
public record Anotacao(
        @Id Long id,
        Long topicoId,
        Long artigoId,
        String conteudo,
        LocalDateTime createdAt) {
}