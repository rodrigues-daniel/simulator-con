package com.cebraspe.repository;

import com.cebraspe.record.Subtema;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubtemaRepository extends CrudRepository<Subtema, Long> {
    List<Subtema> findByTemaId(Long temaId);

    @Query("SELECT * FROM subtema WHERE tema_id = :temaId AND ativo = true")
    List<Subtema> findByTemaIdAndAtivoTrue(Long temaId);

    @Query("SELECT * FROM subtema WHERE ativo = true ORDER BY tema_id, nome")
    List<Subtema> findAllAtivos();

    Optional<Subtema> findByTemaIdAndNomeIgnoreCase(Long temaId, String nome);

    @Modifying
    @Query("UPDATE subtema SET nome=:nome, ativo=:ativo WHERE id=:id")
    void atualizar(Long id, String nome, Boolean ativo);
}