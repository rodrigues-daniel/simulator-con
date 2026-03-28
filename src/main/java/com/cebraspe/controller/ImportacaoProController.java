package com.cebraspe.controller;

import com.cebraspe.record.response.ImportacaoProResponse;
import com.cebraspe.service.ImportacaoProService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/importar")
public class ImportacaoProController {

    private final ImportacaoProService service;

    public ImportacaoProController(ImportacaoProService service) {
        this.service = service;
    }

    @PostMapping(consumes = "text/plain")
    public ResponseEntity<ImportacaoProResponse> importarTexto(@RequestBody String json) {
        return ResponseEntity.ok(service.importar(json));
    }

    @PostMapping(consumes = "application/json")
    public ResponseEntity<ImportacaoProResponse> importarJson(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(service.importar(body.get("json")));
    }
}