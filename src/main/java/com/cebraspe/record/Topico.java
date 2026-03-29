package com.cebraspe.record;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("topico")
public record Topico(
        @Id Long id,
        Long materiaId,
        String nome,
        String artigoBase,
        String resumo,
        Integer ordem,
        Boolean ativo) {
}