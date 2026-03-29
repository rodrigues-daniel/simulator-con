package com.cebraspe.record;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;

@Table("materia")
public record Materia(
        @Id Long id,
        Long cargoId,
        String nome,
        String descricao,
        String fonteLegal,
        Integer ordem,
        Boolean ativa,
        LocalDateTime createdAt) {
}