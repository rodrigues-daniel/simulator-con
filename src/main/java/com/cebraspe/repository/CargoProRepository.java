package com.cebraspe.repository;

import com.cebraspe.record.CargoPro;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CargoProRepository extends CrudRepository<CargoPro, Long> {
    List<CargoPro> findByAtivoTrue();

    Optional<CargoPro> findByNomeIgnoreCase(String nome);

    @Modifying
    @Query("UPDATE cargo_pro SET nome=:nome, orgao=:orgao, ativo=:ativo WHERE id=:id")
    void atualizar(Long id, String nome, String orgao, Boolean ativo);
}