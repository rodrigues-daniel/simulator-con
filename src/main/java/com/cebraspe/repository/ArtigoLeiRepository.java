package com.cebraspe.repository;

import com.cebraspe.record.ArtigoLei;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ArtigoLeiRepository extends CrudRepository<ArtigoLei, Long> {

    @Query("SELECT * FROM artigo_lei WHERE topico_id = :topicoId ORDER BY ordem")
    List<ArtigoLei> findByTopicoId(Long topicoId);

    @Query("SELECT * FROM artigo_lei WHERE topico_id = :topicoId AND destaque = true ORDER BY ordem")
    List<ArtigoLei> findDestaquesByTopicoId(Long topicoId);
}