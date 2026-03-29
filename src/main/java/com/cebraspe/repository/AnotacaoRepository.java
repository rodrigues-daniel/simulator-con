package com.cebraspe.repository;

import com.cebraspe.record.Anotacao;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AnotacaoRepository extends CrudRepository<Anotacao, Long> {

    @Query("SELECT * FROM anotacao WHERE topico_id = :topicoId ORDER BY created_at DESC")
    List<Anotacao> findByTopicoId(Long topicoId);
}