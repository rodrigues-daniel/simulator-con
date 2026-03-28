package com.cebraspe.record;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("subtema")
public record Subtema(
        @Id Long id,
        Long temaId,
        String nome,
        Boolean ativo) {
}