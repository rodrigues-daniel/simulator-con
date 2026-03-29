package com.cebraspe.repository;

import com.cebraspe.record.QuestaoLei;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuestaoLeiRepository extends CrudRepository<QuestaoLei, Long> {

    @Query("""
                SELECT * FROM questao_lei
                WHERE topico_id = :topicoId AND ativa = true
                ORDER BY recorrencia DESC, nivel, id
            """)
    List<QuestaoLei> findByTopicoId(Long topicoId);

    @Query("SELECT COUNT(*) FROM questao_lei WHERE topico_id = :topicoId AND ativa = true")
    int countByTopicoId(Long topicoId);

    @Query("SELECT COUNT(*) FROM questao_lei WHERE ativa = true")
    long countAtivas();
}