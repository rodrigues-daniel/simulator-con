package com.cebraspe.repository;

import com.cebraspe.record.SessaoProva;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SessaoProvaRepository extends CrudRepository<SessaoProva, Long> {

    @Query("SELECT * FROM sessao_prova ORDER BY created_at DESC LIMIT 20")
    List<SessaoProva> findRecentes();

    @Modifying
    @Query("""
                UPDATE sessao_prova SET
                    status = :status, acertos = :acertos, erros = :erros,
                    tempo_total_ms = :tempoTotalMs, finalizada_at = NOW()
                WHERE id = :id
            """)
    void finalizar(Long id, String status, int acertos, int erros, Long tempoTotalMs);
}