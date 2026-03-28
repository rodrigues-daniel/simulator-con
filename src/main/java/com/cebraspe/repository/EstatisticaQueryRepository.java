package com.cebraspe.repository;

import com.cebraspe.record.dto.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EstatisticaQueryRepository {

    private final JdbcTemplate jdbc;

    public EstatisticaQueryRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /* ── Pool de questões (movido para cá por usar subquery dinâmica) ── */

    private static final RowMapper<Long> ID_MAPPER = (rs, row) -> rs.getLong("id");

    /**
     * Busca questões por nível excluindo as já respondidas corretamente.
     */
    public List<Long> idsQuestoesPorNivel(
            String grupo, String nivel, Long cargoId, int limite) {
        return jdbc.query("""
                SELECT id FROM questao_pro
                WHERE ativa = true
                  AND grupo = ?
                  AND nivel_dificuldade = ?
                  AND id NOT IN (SELECT questao_id FROM questao_respondida)
                  AND (? IS NULL OR cargo_id = ? OR cargo_id IS NULL)
                ORDER BY RANDOM()
                LIMIT ?
                """,
                ID_MAPPER,
                grupo, nivel, cargoId, cargoId, limite);
    }

    /**
     * Busca questões aleatórias excluindo as já respondidas.
     */
    public List<Long> idsQuestoesAleatorio(
            String grupo, Long cargoId, int limite) {
        return jdbc.query("""
                SELECT id FROM questao_pro
                WHERE ativa = true
                  AND grupo = ?
                  AND id NOT IN (SELECT questao_id FROM questao_respondida)
                  AND (? IS NULL OR cargo_id = ? OR cargo_id IS NULL)
                ORDER BY RANDOM()
                LIMIT ?
                """,
                ID_MAPPER,
                grupo, cargoId, cargoId, limite);
    }

    /**
     * Busca questões priorizando subtemas com baixa taxa de acerto.
     */
    public List<Long> idsQuestoesDificuldades(
            String grupo, Long cargoId, int limite) {
        return jdbc.query("""
                SELECT q.id
                FROM questao_pro q
                LEFT JOIN (
                    SELECT questao_id,
                           SUM(CASE WHEN acertou THEN 1.0 ELSE 0.0 END)
                           / COUNT(*) AS taxa
                    FROM resposta_pro
                    GROUP BY questao_id
                    HAVING COUNT(*) >= 2
                ) stats ON q.id = stats.questao_id
                WHERE q.ativa = true
                  AND q.grupo = ?
                  AND q.id NOT IN (SELECT questao_id FROM questao_respondida)
                  AND (? IS NULL OR q.cargo_id = ? OR q.cargo_id IS NULL)
                ORDER BY COALESCE(stats.taxa, 0.5) ASC, RANDOM()
                LIMIT ?
                """,
                ID_MAPPER,
                grupo, cargoId, cargoId, limite);
    }

    /* ── Mappers ───────────────────────────────────────────────────── */

    private static final RowMapper<EstatisticaTemaDto> TEMA_MAPPER = (rs, row) -> new EstatisticaTemaDto(
            rs.getLong("tema_id"),
            rs.getLong("total"),
            rs.getLong("acertos"),
            rs.getDouble("tempo_medio_ms"));

    private static final RowMapper<EstatisticaSubtemaDto> SUBTEMA_MAPPER = (rs, row) -> new EstatisticaSubtemaDto(
            rs.getLong("subtema_id"),
            rs.getLong("total"),
            rs.getLong("acertos"),
            rs.getDouble("tempo_medio_ms"));

    private static final RowMapper<EstatisticaNivelDto> NIVEL_MAPPER = (rs, row) -> new EstatisticaNivelDto(
            rs.getString("nivel"),
            rs.getLong("total"),
            rs.getLong("acertos"));

    private static final RowMapper<EstatisticaQuestaoDto> QUESTAO_MAPPER = (rs, row) -> new EstatisticaQuestaoDto(
            rs.getLong("questao_id"),
            rs.getLong("total"),
            rs.getLong("acertos"),
            rs.getDouble("tempo_medio_ms"));

    private static final RowMapper<AcertoSessaoDto> ACERTO_SESSAO_MAPPER = (rs, row) -> new AcertoSessaoDto(
            rs.getLong("questao_id"),
            rs.getBoolean("acertou"));

    private static final RowMapper<SubtemaDificuldadeDto> SUBTEMA_DIFIC_MAPPER = (rs, row) -> new SubtemaDificuldadeDto(
            rs.getLong("subtema_id"),
            rs.getDouble("taxa_acerto"));

    /* ── Queries ───────────────────────────────────────────────────── */

    /**
     * Desempenho agrupado por tema.
     */
    public List<EstatisticaTemaDto> estatisticasPorTema() {
        return jdbc.query("""
                SELECT
                    tema_id,
                    COUNT(*)                                              AS total,
                    SUM(CASE WHEN acertou THEN 1 ELSE 0 END)             AS acertos,
                    COALESCE(AVG(tempo_ms), 0)                           AS tempo_medio_ms
                FROM resposta_pro
                WHERE tema_id IS NOT NULL
                GROUP BY tema_id
                ORDER BY tema_id
                """, TEMA_MAPPER);
    }

    /**
     * Piores 10 subtemas (mínimo 2 respostas, menor taxa de acerto primeiro).
     */
    public List<EstatisticaSubtemaDto> pioresSubtemas() {
        return jdbc.query("""
                SELECT
                    subtema_id,
                    COUNT(*)                                              AS total,
                    SUM(CASE WHEN acertou THEN 1 ELSE 0 END)             AS acertos,
                    COALESCE(AVG(tempo_ms), 0)                           AS tempo_medio_ms
                FROM resposta_pro
                WHERE subtema_id IS NOT NULL
                GROUP BY subtema_id
                HAVING COUNT(*) >= 2
                ORDER BY
                    SUM(CASE WHEN acertou THEN 1.0 ELSE 0.0 END)
                    / COUNT(*) ASC
                LIMIT 10
                """, SUBTEMA_MAPPER);
    }

    /**
     * Desempenho agrupado por nível de dificuldade.
     */
    public List<EstatisticaNivelDto> estatisticasPorNivel() {
        return jdbc.query("""
                SELECT
                    nivel,
                    COUNT(*)                                              AS total,
                    SUM(CASE WHEN acertou THEN 1 ELSE 0 END)             AS acertos
                FROM resposta_pro
                GROUP BY nivel
                ORDER BY
                    CASE nivel
                        WHEN 'FACIL'   THEN 1
                        WHEN 'MEDIO'   THEN 2
                        WHEN 'DIFICIL' THEN 3
                        ELSE 4
                    END
                """, NIVEL_MAPPER);
    }

    /**
     * Estatísticas por questão individual (para listar questões erradas com
     * detalhe).
     */
    public List<EstatisticaQuestaoDto> estatisticasPorQuestao() {
        return jdbc.query("""
                SELECT
                    questao_id,
                    COUNT(*)                                              AS total,
                    SUM(CASE WHEN acertou THEN 1 ELSE 0 END)             AS acertos,
                    COALESCE(AVG(tempo_ms), 0)                           AS tempo_medio_ms
                FROM resposta_pro
                GROUP BY questao_id
                ORDER BY questao_id
                """, QUESTAO_MAPPER);
    }

    /**
     * Para uma sessão específica: retorna (questaoId, acertou) de cada resposta.
     * Usado para bloquear questões corretas após finalizar.
     */
    public List<AcertoSessaoDto> acertosPorSessao(Long sessaoId) {
        return jdbc.query("""
                SELECT questao_id, acertou
                FROM resposta_pro
                WHERE sessao_id = ?
                """, ACERTO_SESSAO_MAPPER, sessaoId);
    }

    /**
     * Subtemas com menor taxa de acerto (mínimo 2 respostas).
     * Usado para montar pool de "focar dificuldades".
     */
    public List<SubtemaDificuldadeDto> subtemasMaisDificeis(int limite) {
        return jdbc.query("""
                SELECT
                    subtema_id,
                    SUM(CASE WHEN acertou THEN 1.0 ELSE 0.0 END)
                    / COUNT(*)  AS taxa_acerto
                FROM resposta_pro
                WHERE subtema_id IS NOT NULL
                GROUP BY subtema_id
                HAVING COUNT(*) >= 2
                ORDER BY taxa_acerto ASC
                LIMIT ?
                """, SUBTEMA_DIFIC_MAPPER, limite);
    }

    /**
     * Total de respostas.
     */
    public long countTotal() {
        Long result = jdbc.queryForObject(
                "SELECT COUNT(*) FROM resposta_pro",
                Long.class);
        return result != null ? result : 0L;
    }

    /**
     * Aproveitamento geral em percentual (0–100).
     */
    public double aproveitamentoGeral() {
        Double result = jdbc.queryForObject("""
                SELECT COALESCE(
                    AVG(CASE WHEN acertou THEN 1.0 ELSE 0.0 END) * 100,
                    0.0
                )
                FROM resposta_pro
                """, Double.class);
        return result != null ? result : 0.0;
    }

    /**
     * Tempo médio por questão em milissegundos.
     */
    public double tempoMedioMs() {
        Double result = jdbc.queryForObject(
                "SELECT COALESCE(AVG(tempo_ms), 0) FROM resposta_pro",
                Double.class);
        return result != null ? result : 0.0;
    }

    /**
     * Número de sessões concluídas.
     */
    public long countSessoesConcluidas() {
        Long result = jdbc.queryForObject(
                "SELECT COUNT(*) FROM sessao_prova WHERE status = 'CONCLUIDA'",
                Long.class);
        return result != null ? result : 0L;
    }

    /**
     * Quantas questões disponíveis (não bloqueadas) por grupo.
     */
    public long countDisponiveisPorGrupo(String grupo) {
        Long result = jdbc.queryForObject("""
                SELECT COUNT(*)
                FROM questao_pro
                WHERE ativa = true
                  AND grupo = ?
                  AND id NOT IN (SELECT questao_id FROM questao_respondida)
                """, Long.class, grupo);
        return result != null ? result : 0L;
    }
}