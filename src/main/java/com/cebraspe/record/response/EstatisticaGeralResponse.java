package com.cebraspe.record.response;

import java.util.List;

public record EstatisticaGeralResponse(
        long totalSessoes,
        long totalRespostas,
        double aproveitamentoGeral,
        double tempoMedioMs,
        List<DesempenhoTemaResponse> porTema,
        List<DesempenhoSubtemaResponse> pioresSubtemas,
        List<DesempenhoNivelResponse> porNivel) {
}