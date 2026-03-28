package com.cebraspe.record.response;

import java.util.List;

public record EstatisticaFixedResponse(
                long totalRespondidas,
                long totalCorretas,
                long totalErradas,
                double aproveitamentoGeral,
                double tempoMedioMs,
                long totalSessoes, // ← adicionado
                List<DesempenhoTemaResponse> porTema,
                List<DesempenhoSubtemaResponse> pioresSubtemas,
                List<DesempenhoNivelResponse> porNivel,
                List<QuestaoErradaDetalhe> questoesErradas,
                long totalBloqueadas // corretas que saíram do pool
) {
}