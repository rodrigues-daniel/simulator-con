package com.cebraspe.controller;

import com.cebraspe.record.Anotacao;
import com.cebraspe.record.QuestaoLei;
import com.cebraspe.record.request.ResponderLeiRequest;
import com.cebraspe.record.response.*;
import com.cebraspe.service.LeiSecaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/lei-seca")
public class LeiSecaController {

    private final LeiSecaService service;

    public LeiSecaController(LeiSecaService service) {
        this.service = service;
    }

    /** Stats gerais do módulo */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> stats(
            @RequestParam(required = false) Long cargoId) {
        return ResponseEntity.ok(service.stats(cargoId));
    }

    /** Lista matérias (com tópicos) filtradas por cargo */
    @GetMapping("/materias")
    public ResponseEntity<List<MateriaResponse>> materias(
            @RequestParam(required = false) Long cargoId) {
        return ResponseEntity.ok(service.listarMaterias(cargoId));
    }

    /** Detalhe de um tópico: artigos + questões */
    @GetMapping("/topicos/{id}")
    public ResponseEntity<TopicoResponse> topico(@PathVariable Long id) {
        return ResponseEntity.ok(service.detalheTopico(id));
    }

    /** Responde uma questão e recebe gabarito + comentário */
    @PostMapping("/responder")
    public ResponseEntity<QuestaoLeiResponse> responder(
            @Valid @RequestBody ResponderLeiRequest req) {
        return ResponseEntity.ok(service.responder(req));
    }

    /** Reseta progresso de um tópico */
    @PostMapping("/topicos/{id}/resetar")
    public ResponseEntity<Void> resetar(@PathVariable Long id) {
        service.resetarTopico(id);
        return ResponseEntity.ok().build();
    }

    /** Salva anotação em um tópico */
    @PostMapping("/topicos/{id}/anotacoes")
    public ResponseEntity<Anotacao> anotacao(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        Long artigoId = body.get("artigoId") != null
                ? Long.parseLong(body.get("artigoId").toString())
                : null;
        return ResponseEntity.ok(
                service.salvarAnotacao(id, artigoId,
                        body.get("conteudo").toString()));
    }

    /** Lista anotações de um tópico */
    @GetMapping("/topicos/{id}/anotacoes")
    public ResponseEntity<List<Anotacao>> anotacoes(@PathVariable Long id) {
        return ResponseEntity.ok(service.listarAnotacoes(id));
    }

    /** Importação em lote via JSON */
    @PostMapping(value = "/importar", consumes = "text/plain")
    public ResponseEntity<ImportacaoLeiResponse> importar(@RequestBody String json) {
        return ResponseEntity.ok(service.importar(json));
    }

    @PostMapping(value = "/importar", consumes = "application/json")
    public ResponseEntity<ImportacaoLeiResponse> importarJson(
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(service.importar(body.get("json")));
    }

    // Adicionar questão diretamente a um tópico pelo id
    @PostMapping("/topicos/{id}/questoes")
    public ResponseEntity<QuestaoLei> adicionarQuestao(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(service.adicionarQuestaoTopico(id, body));
    }
}