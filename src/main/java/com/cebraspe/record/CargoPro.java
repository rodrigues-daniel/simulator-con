package com.cebraspe.record;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;

@Table("cargo_pro")
public record CargoPro(
        @Id Long id,
        String nome,
        String orgao,
        Boolean ativo,
        LocalDateTime createdAt) {
}