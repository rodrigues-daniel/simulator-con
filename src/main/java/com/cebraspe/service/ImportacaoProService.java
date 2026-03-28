package com.cebraspe.service;

import com.cebraspe.record.*;
import com.cebraspe.record.response.ImportacaoProResponse;
import com.cebraspe.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ImportacaoProService {

    private static final Map<String, String> NIVEL_MAP = Map.of(
            "fácil", "FACIL",
            "facil", "FACIL",
            "easy", "FACIL",
            "médio", "MEDIO",
            "medio", "MEDIO",
            "medium", "MEDIO",
            "difícil", "DIFICIL",
            "dificil", "DIFICIL",
            "hard", "DIFICIL");

    private final QuestaoProRepository questaoRepo;
    private final CargoProRepository cargoRepo;
    private final TemaRepository temaRepo;
    private final SubtemaRepository subtemaRepo;
    private final ObjectMapper mapper;

    public ImportacaoProService(QuestaoProRepository questaoRepo,
            CargoProRepository cargoRepo,
            TemaRepository temaRepo,
            SubtemaRepository subtemaRepo,
            ObjectMapper mapper) {
        this.questaoRepo = questaoRepo;
        this.cargoRepo = cargoRepo;
        this.temaRepo = temaRepo;
        this.subtemaRepo = subtemaRepo;
        this.mapper = mapper;
    }

    @Transactional
    public ImportacaoProResponse importar(String jsonBruto) {
        List<String> erros = new ArrayList<>();
        int importadas = 0, ignoradas = 0, temasNovos = 0, subtemasNovos = 0;

        JsonNode arr;
        try {
            String limpo = jsonBruto.strip()
                    .replaceAll("(?s)^```[a-zA-Z]*\\n?", "")
                    .replaceAll("```$", "").strip();
            arr = mapper.readTree(limpo);
        } catch (Exception e) {
            return new ImportacaoProResponse(0, 0, 0, 0, 0,
                    List.of("JSON inválido: " + e.getMessage()));
        }

        if (!arr.isArray()) {
            return new ImportacaoProResponse(0, 0, 0, 0, 0,
                    List.of("Esperado array JSON [ { ... } ]"));
        }

        int total = arr.size();

        for (int i = 0; i < total; i++) {
            JsonNode node = arr.get(i);
            String ctx = "Item #" + (i + 1);
            try {
                // Campos obrigatórios
                String temaNome = txt(node, "tema");
                String subtemaNome = txt(node, "subtema");
                String enunciado = txt(node, "enunciado");
                String explicacao = txt(node, "explicacao");
                // Na importação, ler o campo "grupo" do payload:
                String grupoRaw = txt(node, "grupo");
                String grupo = resolverGrupo(grupoRaw);

                if (blank(temaNome)) {
                    erros.add(ctx + ": 'tema' obrigatório");
                    ignoradas++;
                    continue;
                }
                if (blank(subtemaNome)) {
                    erros.add(ctx + ": 'subtema' obrigatório");
                    ignoradas++;
                    continue;
                }
                if (blank(enunciado)) {
                    erros.add(ctx + ": 'enunciado' obrigatório");
                    ignoradas++;
                    continue;
                }
                if (blank(explicacao)) {
                    erros.add(ctx + ": 'explicacao' obrigatório");
                    ignoradas++;
                    continue;
                }

                // Gabarito — aceita boolean e string "certo"/"errado"
                Boolean gabarito = resolverGabarito(node);
                if (gabarito == null) {
                    erros.add(ctx + ": 'gabarito' deve ser true/false ou 'certo'/'errado'");
                    ignoradas++;
                    continue;
                }

                // Nível
                String nivelRaw = txt(node, "nivel_dificuldade");
                String nivel = resolverNivel(nivelRaw);

                // Campos opcionais
                String tipoPegadinha = txt(node, "tipoPegadinha");
                String artigoRef = txt(node, "artigoRef");
                String cargoNome = txt(node, "cargo");

                // Resolver / criar Tema
                Tema tema = temaRepo.findByNomeIgnoreCase(temaNome).orElse(null);
                if (tema == null) {
                    tema = temaRepo.save(new Tema(null, temaNome, true, LocalDateTime.now()));
                    temasNovos++;
                }

                // Resolver / criar Subtema
                Subtema subtema = subtemaRepo.findByTemaIdAndNomeIgnoreCase(tema.id(), subtemaNome).orElse(null);
                if (subtema == null) {
                    subtema = subtemaRepo.save(new Subtema(null, tema.id(), subtemaNome, true));
                    subtemasNovos++;
                }

                // Resolver Cargo (opcional)
                Long cargoId = null;
                if (!blank(cargoNome)) {
                    String cn = cargoNome;
                    cargoId = cargoRepo.findByNomeIgnoreCase(cn).map(CargoPro::id).orElse(null);
                    if (cargoId == null)
                        erros.add(ctx + ": cargo '" + cn + "' não encontrado, importado sem cargo");
                }

                questaoRepo.save(new QuestaoProva(
                        null, cargoId, tema.id(), subtema.id(),
                        enunciado, gabarito, explicacao,
                        tipoPegadinha, artigoRef, nivel,
                        true, grupo, LocalDateTime.now(), LocalDateTime.now()));
                importadas++;

            } catch (Exception e) {
                erros.add(ctx + ": " + e.getMessage());
                ignoradas++;
            }
        }

        return new ImportacaoProResponse(total, importadas, ignoradas,
                temasNovos, subtemasNovos, erros);
    }

    private Boolean resolverGabarito(JsonNode node) {
        if (!node.has("gabarito") || node.get("gabarito").isNull())
            return null;
        JsonNode g = node.get("gabarito");
        if (g.isBoolean())
            return g.asBoolean();
        String s = g.asText("").toLowerCase().strip();
        return switch (s) {
            case "certo", "true", "sim", "c" -> true;
            case "errado", "false", "nao", "não", "e" -> false;
            default -> null;
        };
    }

    private String resolverNivel(String raw) {
        if (raw == null || raw.isBlank())
            return "MEDIO";
        String key = raw.toLowerCase().strip()
                .replace("á", "a").replace("é", "e").replace("í", "i")
                .replace("ó", "o").replace("ú", "u").replace("ã", "a");
        return NIVEL_MAP.getOrDefault(key, "MEDIO");
    }

    private String txt(JsonNode n, String campo) {
        if (!n.has(campo) || n.get(campo).isNull())
            return null;
        String v = n.get(campo).asText("").strip();
        return v.isEmpty() ? null : v;
    }

    private boolean blank(String s) {
        return s == null || s.isBlank();
    }

    // RESOLVE A ENTRADA DE GRUPO PARA AS QUESTÕES PROVA, COM BASE NO CAMPO "grupo"
    // DO JSON
    // Depois implementar para banco de dados, para manter a consistência-
    private String resolverGrupo(String raw) {
        if (raw == null || raw.isBlank())
            return "EDITAL";
        return switch (raw.toUpperCase().strip()) {
            case "BANCA", "PROVA_REAL", "REAL" -> "BANCA";
            default -> "EDITAL";
        };
    }
}