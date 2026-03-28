package com.cebraspe.repository;

import com.cebraspe.record.Tema;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TemaRepository extends CrudRepository<Tema, Long> {
    List<Tema> findByAtivoTrue();

    Optional<Tema> findByNomeIgnoreCase(String nome);

    @Modifying
    @Query("UPDATE tema SET nome=:nome, ativo=:ativo WHERE id=:id")
    void atualizar(Long id, String nome, Boolean ativo);
}