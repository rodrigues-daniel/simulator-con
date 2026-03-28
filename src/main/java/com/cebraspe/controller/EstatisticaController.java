package com.cebraspe.controller;

import com.cebraspe.record.response.EstatisticaFixedResponse;
import com.cebraspe.service.EstatisticaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/estatisticas")
public class EstatisticaController {

    private final EstatisticaService service;

    public EstatisticaController(EstatisticaService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<EstatisticaFixedResponse> geral() {
        return ResponseEntity.ok(service.gerarEstatisticas());
    }

    @GetMapping("/focos")
    public ResponseEntity<List<Long>> focos(
            @RequestParam(defaultValue = "5") int limite) {
        return ResponseEntity.ok(service.subtemasMaisDificeis(limite));
    }

    @GetMapping("/disponiveis")
    public ResponseEntity<Map<String, Long>> disponiveis() {
        return ResponseEntity.ok(Map.of(
                "EDITAL", service.countDisponiveisParaGrupo("EDITAL"),
                "BANCA", service.countDisponiveisParaGrupo("BANCA")));
    }

    @PostMapping("/resetar")
    public ResponseEntity<Void> resetar() {
        service.resetarQuestoes();
        return ResponseEntity.ok().build();
    }
}