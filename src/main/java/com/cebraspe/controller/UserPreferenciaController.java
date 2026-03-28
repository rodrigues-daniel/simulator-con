package com.cebraspe.controller;

import com.cebraspe.service.UserPreferenciaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/preferencias")
public class UserPreferenciaController {

    private final UserPreferenciaService service;

    public UserPreferenciaController(UserPreferenciaService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Map<String, String>> listar() {
        return ResponseEntity.ok(service.listarTodas());
    }

    @PutMapping("/{chave}")
    public ResponseEntity<Void> salvar(
            @PathVariable String chave,
            @RequestBody Map<String, String> body) {
        service.salvar(chave, body.get("valor"));
        return ResponseEntity.ok().build();
    }
}