package com.cebraspe.repository;

import com.cebraspe.record.ProgressoTopico;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ProgressoTopicoRepository extends CrudRepository<ProgressoTopico, Long> {

    Optional<ProgressoTopico> findByTopicoId(Long topicoId);

    @Modifying
    @Query("""
                INSERT INTO progresso_topico (topico_id, status, acertos, erros, ultima_revisao, updated_at)
                VALUES (:topicoId, :status, :acertos, :erros, NOW(), NOW())
                ON CONFLICT (topico_id) DO UPDATE
                SET status       = EXCLUDED.status,
                    acertos      = EXCLUDED.acertos,
                    erros        = EXCLUDED.erros,
                    ultima_revisao = NOW(),
                    updated_at   = NOW()
            """)
    void upsert(Long topicoId, String status, int acertos, int erros);

    @Modifying
    @Query("DELETE FROM progresso_topico WHERE topico_id = :topicoId")
    void resetarPorTopico(Long topicoId);

    @Modifying
    @Query("DELETE FROM progresso_topico")
    void resetarTudo();
}