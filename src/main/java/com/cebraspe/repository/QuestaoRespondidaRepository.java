package com.cebraspe.repository;

import com.cebraspe.record.QuestaoRespondida;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuestaoRespondidaRepository
        extends CrudRepository<QuestaoRespondida, Long> {

    @Query("SELECT questao_id FROM questao_respondida")
    List<Long> findAllQuestaoIds();

    @Query("SELECT COUNT(*) FROM questao_respondida")
    long countBloqueadas();

    @Modifying
    @Query("DELETE FROM questao_respondida")
    void resetarTodas();

    @Query("SELECT EXISTS(SELECT 1 FROM questao_respondida WHERE questao_id = :questaoId)")
    boolean existsByQuestaoId(Long questaoId);
}