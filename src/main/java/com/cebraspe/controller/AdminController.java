package com.cebraspe.controller;

import com.cebraspe.record.*;
import com.cebraspe.record.request.SalvarQuestaoRequest;
import com.cebraspe.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService service;

    public AdminController(AdminService service) {
        this.service = service;
    }

    @GetMapping("/resumo")
    public ResponseEntity<Map<String, Long>> resumo() {
        return ResponseEntity.ok(service.resumo());
    }

    // ── CARGOS ──
    @GetMapping("/cargos")
    public ResponseEntity<List<CargoPro>> cargos() {
        return ResponseEntity.ok(service.listarCargos());
    }

    @PostMapping("/cargos")
    public ResponseEntity<CargoPro> criarCargo(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(service.criarCargo(body.get("nome"), body.get("orgao")));
    }

    @PutMapping("/cargos/{id}")
    public ResponseEntity<Void> editarCargo(@PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        service.editarCargo(id, (String) body.get("nome"), (String) body.get("orgao"),
                body.get("ativo") == null || (Boolean) body.get("ativo"));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/cargos/{id}")
    public ResponseEntity<Void> deletarCargo(@PathVariable Long id) {
        service.deletarCargo(id);
        return ResponseEntity.noContent().build();
    }

    // ── TEMAS ──
    @GetMapping("/temas")
    public ResponseEntity<List<Tema>> temas() {
        return ResponseEntity.ok(service.listarTemas());
    }

    @PostMapping("/temas")
    public ResponseEntity<Tema> criarTema(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(service.criarTema(body.get("nome")));
    }

    @PutMapping("/temas/{id}")
    public ResponseEntity<Void> editarTema(@PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        service.editarTema(id, (String) body.get("nome"),
                body.get("ativo") == null || (Boolean) body.get("ativo"));
        return ResponseEntity.ok().build();
    }

    // ── SUBTEMAS ──
    @GetMapping("/subtemas")
    public ResponseEntity<List<Subtema>> subtemas(
            @RequestParam(required = false) Long temaId) {
        return ResponseEntity.ok(service.listarSubtemas(temaId));
    }

    @PostMapping("/subtemas")
    public ResponseEntity<Subtema> criarSubtema(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(service.criarSubtema(
                Long.parseLong(body.get("temaId").toString()), (String) body.get("nome")));
    }

    @PutMapping("/subtemas/{id}")
    public ResponseEntity<Void> editarSubtema(@PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        service.editarSubtema(id, (String) body.get("nome"),
                body.get("ativo") == null || (Boolean) body.get("ativo"));
        return ResponseEntity.ok().build();
    }

    // ── QUESTÕES ──
    @GetMapping("/questoes")
    public ResponseEntity<List<QuestaoProva>> questoes() {
        return ResponseEntity.ok(service.listarQuestoes());
    }

    @PostMapping("/questoes")
    public ResponseEntity<QuestaoProva> criarQuestao(
            @Valid @RequestBody SalvarQuestaoRequest req) {
        return ResponseEntity.ok(service.criarQuestao(req));
    }

    @PutMapping("/questoes/{id}")
    public ResponseEntity<Void> editarQuestao(@PathVariable Long id,
            @Valid @RequestBody SalvarQuestaoRequest req) {
        service.editarQuestao(id, req);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/questoes/{id}")
    public ResponseEntity<Void> deletarQuestao(@PathVariable Long id) {
        service.deletarQuestao(id);
        return ResponseEntity.noContent().build();
    }
}