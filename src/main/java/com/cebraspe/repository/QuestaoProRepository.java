package com.cebraspe.repository;

import com.cebraspe.record.QuestaoProva;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuestaoProRepository extends CrudRepository<QuestaoProva, Long> {

        // ── Leitura simples (Spring Data deriva do nome) ──────────────────────────

        @Query("SELECT * FROM questao_pro WHERE ativa = true ORDER BY created_at DESC")
        List<QuestaoProva> findAllAtivas();

        @Query("SELECT COUNT(*) FROM questao_pro WHERE ativa = true")
        long countAtivas();

        @Query("""
                            SELECT COUNT(*) FROM questao_pro
                            WHERE ativa = true AND nivel_dificuldade = :nivel
                        """)
        long countPorNivel(String nivel);

        // ── Update ────────────────────────────────────────────────────────────────

        @Modifying
        @Query("""
                            UPDATE questao_pro SET
                                cargo_id          = :cargoId,
                                tema_id           = :temaId,
                                subtema_id        = :subtemaId,
                                enunciado         = :enunciado,
                                gabarito          = :gabarito,
                                explicacao        = :explicacao,
                                tipo_pegadinha    = :tipoPegadinha,
                                artigo_ref        = :artigoRef,
                                nivel_dificuldade = :nivelDificuldade,
                                grupo             = :grupo,
                                ativa             = :ativa,
                                updated_at        = NOW()
                            WHERE id = :id
                        """)
        void atualizar(Long id, Long cargoId, Long temaId, Long subtemaId,
                        String enunciado, Boolean gabarito, String explicacao,
                        String tipoPegadinha, String artigoRef,
                        String nivelDificuldade, String grupo, Boolean ativa);
}
