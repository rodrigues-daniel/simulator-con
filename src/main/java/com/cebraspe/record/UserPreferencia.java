package com.cebraspe.record;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;

@Table("user_preferencia")
public record UserPreferencia(
        @Id String chave,
        String valor,
        LocalDateTime updatedAt) {
}