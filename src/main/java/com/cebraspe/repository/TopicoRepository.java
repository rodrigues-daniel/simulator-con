package com.cebraspe.repository;

import com.cebraspe.record.Topico;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TopicoRepository extends CrudRepository<Topico, Long> {

    @Query("SELECT * FROM topico WHERE materia_id = :materiaId AND ativo = true ORDER BY ordem, nome")
    List<Topico> findByMateriaIdAtivos(Long materiaId);

    Optional<Topico> findByMateriaIdAndNomeIgnoreCase(Long materiaId, String nome);

    @Modifying
    @Query("UPDATE topico SET resumo = :resumo WHERE id = :id")
    void atualizarResumo(Long id, String resumo);
}