package com.cebraspe.record;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("artigo_lei")
public record ArtigoLei(
        @Id Long id,
        Long topicoId,
        String identificador,
        String texto,
        Boolean destaque,
        Integer ordem) {
}