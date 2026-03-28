package com.cebraspe.service;

import com.cebraspe.record.UserPreferencia;
import com.cebraspe.repository.UserPreferenciaRepository;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserPreferenciaService {

    private final UserPreferenciaRepository repo;

    public UserPreferenciaService(UserPreferenciaRepository repo) {
        this.repo = repo;
    }

    public Map<String, String> listarTodas() {
        Map<String, String> mapa = new HashMap<>();
        repo.findAll().forEach(p -> mapa.put(p.chave(), p.valor()));
        return mapa;
    }

    public void salvar(String chave, String valor) {
        repo.upsert(chave, valor);
    }

    public String obter(String chave, String padrao) {
        return repo.findById(chave).map(UserPreferencia::valor).orElse(padrao);
    }
}