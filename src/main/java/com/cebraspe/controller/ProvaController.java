package com.cebraspe.controller;

import com.cebraspe.record.SessaoProva;
import com.cebraspe.record.request.CriarSessaoRequest;
import com.cebraspe.record.request.ResponderRequest;
import com.cebraspe.record.response.RespostaProResponse;
import com.cebraspe.record.response.SessaoIniciadaResponse;
import com.cebraspe.service.ProvaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/prova")
public class ProvaController {

    private final ProvaService service;

    public ProvaController(ProvaService service) {
        this.service = service;
    }

    @PostMapping("/sessao")
    public ResponseEntity<SessaoIniciadaResponse> criarSessao(
            @Valid @RequestBody CriarSessaoRequest req) throws Exception {
        return ResponseEntity.ok(service.criarSessao(req));
    }

    @PostMapping("/sessao/{sessaoId}/responder")
    public ResponseEntity<RespostaProResponse> responder(
            @PathVariable Long sessaoId,
            @Valid @RequestBody ResponderRequest req) {
        return ResponseEntity.ok(service.responder(sessaoId, req));
    }

    @PostMapping("/sessao/{sessaoId}/finalizar")
    public ResponseEntity<Map<String, Object>> finalizar(@PathVariable Long sessaoId) {
        return ResponseEntity.ok(service.finalizarSessao(sessaoId));
    }

    // @GetMapping("/sessoes")
    // public ResponseEntity<List<SessaoProva>> sessoes() {
    // return ResponseEntity.ok(service.listarSessoes());
    // }
}