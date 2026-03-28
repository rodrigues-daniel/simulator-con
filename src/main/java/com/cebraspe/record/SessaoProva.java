package com.cebraspe.record;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;

@Table("sessao_prova")
public record SessaoProva(
        @Id Long id,
        Long cargoId,
        String configJson,
        String status,
        Integer totalQuestoes,
        Integer acertos,
        Integer erros,
        Long tempoTotalMs,
        LocalDateTime createdAt,
        LocalDateTime finalizadaAt) {
}