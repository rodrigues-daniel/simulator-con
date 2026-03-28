package com.cebraspe.service;

import com.cebraspe.record.*;
import com.cebraspe.record.request.SalvarQuestaoRequest;
import com.cebraspe.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    private final CargoProRepository cargoRepo;
    private final TemaRepository temaRepo;
    private final SubtemaRepository subtemaRepo;
    private final QuestaoProRepository questaoRepo;

    public AdminService(CargoProRepository cargoRepo, TemaRepository temaRepo,
            SubtemaRepository subtemaRepo, QuestaoProRepository questaoRepo) {
        this.cargoRepo = cargoRepo;
        this.temaRepo = temaRepo;
        this.subtemaRepo = subtemaRepo;
        this.questaoRepo = questaoRepo;
    }

    // ── CARGOS ──
    public List<CargoPro> listarCargos() {
        return cargoRepo.findByAtivoTrue();
    }

    @Transactional
    public CargoPro criarCargo(String nome, String orgao) {
        return cargoRepo.save(new CargoPro(null, nome, orgao, true, LocalDateTime.now()));
    }

    @Transactional
    public void editarCargo(Long id, String nome, String orgao, Boolean ativo) {
        cargoRepo.atualizar(id, nome, orgao, ativo);
    }

    @Transactional
    public void deletarCargo(Long id) {
        cargoRepo.atualizar(id, null, null, false);
    }

    // ── TEMAS ──
    public List<Tema> listarTemas() {
        return temaRepo.findByAtivoTrue();
    }

    @Transactional
    public Tema criarTema(String nome) {
        return temaRepo.save(new Tema(null, nome, true, LocalDateTime.now()));
    }

    @Transactional
    public void editarTema(Long id, String nome, Boolean ativo) {
        temaRepo.atualizar(id, nome, ativo);
    }

    // ── SUBTEMAS ──
    public List<Subtema> listarSubtemas(Long temaId) {
        return temaId != null
                ? subtemaRepo.findByTemaIdAndAtivoTrue(temaId)
                : subtemaRepo.findAllAtivos();
    }

    @Transactional
    public Subtema criarSubtema(Long temaId, String nome) {
        return subtemaRepo.save(new Subtema(null, temaId, nome, true));
    }

    @Transactional
    public void editarSubtema(Long id, String nome, Boolean ativo) {
        subtemaRepo.atualizar(id, nome, ativo);
    }

    // ── QUESTÕES ──
    public List<QuestaoProva> listarQuestoes() {
        return questaoRepo.findAllAtivas();
    }

    @Transactional
    public QuestaoProva criarQuestao(SalvarQuestaoRequest req) {
        return questaoRepo.save(new QuestaoProva(
                null, req.cargoId(), req.temaId(), req.subtemaId(),
                req.enunciado(), req.gabarito(), req.explicacao(),
                req.tipoPegadinha(), req.artigoRef(),
                req.nivelDificuldade().toUpperCase(),
                true, req.grupo(), LocalDateTime.now(), LocalDateTime.now()));
    }

    @Transactional
    public void editarQuestao(Long id, SalvarQuestaoRequest req) {
        questaoRepo.atualizar(id, req.cargoId(), req.temaId(), req.subtemaId(),
                req.enunciado(), req.gabarito(), req.explicacao(),
                req.tipoPegadinha(), req.artigoRef(),
                req.nivelDificuldade().toUpperCase(), "AdminService", true);
    }

    @Transactional
    public void deletarQuestao(Long id) {
        QuestaoProva q = questaoRepo.findById(id).orElseThrow();
        questaoRepo.atualizar(id, q.cargoId(), q.temaId(), q.subtemaId(),
                q.enunciado(), q.gabarito(), q.explicacao(),
                q.tipoPegadinha(), q.artigoRef(), q.nivelDificuldade(), "AdminService", false);
    }

    public Map<String, Long> resumo() {
        return Map.of(
                "questoes", questaoRepo.countAtivas(),
                "facil", questaoRepo.countPorNivel("FACIL"),
                "medio", questaoRepo.countPorNivel("MEDIO"),
                "dificil", questaoRepo.countPorNivel("DIFICIL"),
                "temas", (long) temaRepo.findByAtivoTrue().size(),
                "subtemas", (long) subtemaRepo.findAllAtivos().size(),
                "cargos", (long) cargoRepo.findByAtivoTrue().size());
    }
}