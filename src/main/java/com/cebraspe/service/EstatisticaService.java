package com.cebraspe.service;

import com.cebraspe.record.*;
import com.cebraspe.record.dto.*;
import com.cebraspe.record.response.*;
import com.cebraspe.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class EstatisticaService {

    private final EstatisticaQueryRepository statsRepo;
    private final QuestaoProRepository questaoRepo;
    private final TemaRepository temaRepo;
    private final SubtemaRepository subtemaRepo;
    private final QuestaoRespondidaRepository bloqueioRepo;

    public EstatisticaService(
            EstatisticaQueryRepository statsRepo,
            QuestaoProRepository questaoRepo,
            TemaRepository temaRepo,
            SubtemaRepository subtemaRepo,
            QuestaoRespondidaRepository bloqueioRepo) {
        this.statsRepo = statsRepo;
        this.questaoRepo = questaoRepo;
        this.temaRepo = temaRepo;
        this.subtemaRepo = subtemaRepo;
        this.bloqueioRepo = bloqueioRepo;
    }

    /* ── Estatística Geral ─────────────────────────────────────────── */

    public EstatisticaFixedResponse gerarEstatisticas() {

        long total = statsRepo.countTotal();
        double aprov = statsRepo.aproveitamentoGeral();
        double tempo = statsRepo.tempoMedioMs();
        long bloq = bloqueioRepo.countBloqueadas();
        long sessoes = statsRepo.countSessoesConcluidas();

        // ── Por Tema ────────────────────────────────────────────────
        List<DesempenhoTemaResponse> porTema = statsRepo.estatisticasPorTema()
                .stream()
                .map(dto -> {
                    String nome = temaRepo.findById(dto.temaId())
                            .map(Tema::nome)
                            .orElse("Tema " + dto.temaId());
                    double pct = dto.total() > 0
                            ? dto.acertos() * 100.0 / dto.total()
                            : 0.0;
                    return new DesempenhoTemaResponse(
                            dto.temaId(), nome,
                            dto.total(), dto.acertos(),
                            pct, dto.tempoMedioMs());
                })
                .sorted(Comparator.comparingDouble(
                        DesempenhoTemaResponse::aproveitamento))
                .toList();

        // ── Piores Subtemas ─────────────────────────────────────────
        List<DesempenhoSubtemaResponse> piores = statsRepo.pioresSubtemas()
                .stream()
                .map(dto -> {
                    Subtema sub = subtemaRepo.findById(dto.subtemaId())
                            .orElse(null);
                    if (sub == null)
                        return null;
                    String temaNome = temaRepo.findById(sub.temaId())
                            .map(Tema::nome).orElse("—");
                    double pct = dto.total() > 0
                            ? dto.acertos() * 100.0 / dto.total()
                            : 0.0;
                    return new DesempenhoSubtemaResponse(
                            dto.subtemaId(), sub.nome(), temaNome,
                            dto.total(), dto.acertos(),
                            pct, dto.tempoMedioMs());
                })
                .filter(Objects::nonNull)
                .toList();

        // ── Por Nível ───────────────────────────────────────────────
        List<DesempenhoNivelResponse> porNivel = statsRepo.estatisticasPorNivel()
                .stream()
                .map(dto -> new DesempenhoNivelResponse(
                        dto.nivel(), dto.total(), dto.acertos(),
                        dto.total() > 0
                                ? dto.acertos() * 100.0 / dto.total()
                                : 0.0))
                .toList();

        // ── Questões Erradas Detalhadas ─────────────────────────────
        List<QuestaoErradaDetalhe> erradas = construirErradas();

        long acertosTotal = Math.round(total * aprov / 100.0);
        long errosTotal = total - acertosTotal;

        return new EstatisticaFixedResponse(
                total, acertosTotal, errosTotal,
                aprov, tempo, sessoes,
                porTema, piores, porNivel,
                erradas, bloq);
    }

    /* ── Questões Erradas ──────────────────────────────────────────── */

    private List<QuestaoErradaDetalhe> construirErradas() {
        return statsRepo.estatisticasPorQuestao()
                .stream()
                .filter(dto -> dto.total() > dto.acertos()) // tem pelo menos 1 erro
                .map(dto -> {
                    QuestaoProva q = questaoRepo.findById(dto.questaoId())
                            .orElse(null);
                    if (q == null || Boolean.FALSE.equals(q.ativa()))
                        return null;

                    String temaNm = temaRepo.findById(q.temaId())
                            .map(Tema::nome).orElse("—");
                    String subNm = subtemaRepo.findById(q.subtemaId())
                            .map(Subtema::nome).orElse("—");

                    long erros = dto.total() - dto.acertos();
                    double taxaAcerto = dto.total() > 0
                            ? dto.acertos() * 100.0 / dto.total()
                            : 0.0;

                    return new QuestaoErradaDetalhe(
                            dto.questaoId(),
                            temaNm, subNm,
                            q.enunciado(), q.gabarito(),
                            q.explicacao(), q.tipoPegadinha(), q.artigoRef(),
                            q.nivelDificuldade(),
                            erros, taxaAcerto, dto.tempoMedioMs());
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingLong(
                        QuestaoErradaDetalhe::vezesErrada).reversed())
                .toList();
    }

    /* ── Bloquear corretas após sessão ─────────────────────────────── */

    @Transactional
    public void marcarCorretasBloqueadas(Long sessaoId) {
        statsRepo.acertosPorSessao(sessaoId)
                .stream()
                .filter(AcertoSessaoDto::acertou)
                .filter(dto -> !bloqueioRepo.existsByQuestaoId(dto.questaoId()))
                .forEach(dto -> bloqueioRepo.save(
                        new QuestaoRespondida(null, dto.questaoId(), LocalDateTime.now())));
    }

    /* ── Subtemas mais difíceis ────────────────────────────────────── */

    public List<Long> subtemasMaisDificeis(int limite) {
        return statsRepo.subtemasMaisDificeis(limite)
                .stream()
                .map(SubtemaDificuldadeDto::subtemaId)
                .toList();
    }

    /* ── Pool ──────────────────────────────────────────────────────── */

    public long countDisponiveisParaGrupo(String grupo) {
        return statsRepo.countDisponiveisPorGrupo(grupo);
    }

    @Transactional
    public void resetarQuestoes() {
        bloqueioRepo.resetarTodas();
    }
}