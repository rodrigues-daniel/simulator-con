package com.cebraspe.repository;

import com.cebraspe.record.RespostaPro;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RespostaProRepository extends CrudRepository<RespostaPro, Long> {

       List<RespostaPro> findBySessaoId(Long sessaoId);

       @Query("SELECT AVG(CASE WHEN acertou THEN 1.0 ELSE 0.0 END) * 100 FROM resposta_pro")
       Double aproveitamentoGeral();

       @Query("SELECT AVG(tempo_ms) FROM resposta_pro")
       Double tempoMedioMs();

       @Query("""
                         SELECT COUNT(*) FROM sessao_prova
                         WHERE status = 'CONCLUIDA'
                     """)
       long countSessoesConcluidas();
}