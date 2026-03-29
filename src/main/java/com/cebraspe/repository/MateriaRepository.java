package com.cebraspe.repository;

import com.cebraspe.record.Materia;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MateriaRepository extends CrudRepository<Materia, Long> {

    @Query("SELECT * FROM materia WHERE ativa = true ORDER BY ordem, nome")
    List<Materia> findAllAtivas();

    @Query("""
                SELECT * FROM materia
                WHERE ativa = true
                  AND (:cargoId IS NULL OR cargo_id = :cargoId OR cargo_id IS NULL)
                ORDER BY ordem, nome
            """)
    List<Materia> findByCargoIdOrGeral(Long cargoId);

    Optional<Materia> findByNomeIgnoreCase(String nome);
}