package com.cebraspe.repository;

import com.cebraspe.record.UserPreferencia;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPreferenciaRepository
        extends CrudRepository<UserPreferencia, String> {

    @Modifying
    @Query("""
                INSERT INTO user_preferencia (chave, valor, updated_at)
                VALUES (:chave, :valor, NOW())
                ON CONFLICT (chave) DO UPDATE
                SET valor = EXCLUDED.valor, updated_at = NOW()
            """)
    void upsert(String chave, String valor);
}