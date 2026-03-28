package com.cebraspe.record;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;

@Table("questao_pro")
public record QuestaoProva(
                @Id Long id,
                Long cargoId,
                Long temaId,
                Long subtemaId,
                String enunciado,
                Boolean gabarito,
                String explicacao,
                String tipoPegadinha,
                String artigoRef,
                String nivelDificuldade, // FACIL | MEDIO | DIFICIL
                Boolean ativa,
                String grupo,
                LocalDateTime createdAt,
                LocalDateTime updatedAt) {
}
