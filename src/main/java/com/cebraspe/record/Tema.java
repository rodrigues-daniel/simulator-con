package com.cebraspe.record;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;

@Table("tema")
public record Tema(
        @Id Long id,
        String nome,
        Boolean ativo,
        LocalDateTime createdAt) {
}