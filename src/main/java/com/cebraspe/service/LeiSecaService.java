package com.cebraspe.service;

import com.cebraspe.record.*;
import com.cebraspe.record.request.ResponderLeiRequest;
import com.cebraspe.record.response.*;
import com.cebraspe.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.StreamSupport;

@Service
public class LeiSecaService {

    private final MateriaRepository materiaRepo;
    private final TopicoRepository topicoRepo;
    private final ArtigoLeiRepository artigoRepo;
    private final QuestaoLeiRepository questaoLeiRepo;
    private final ProgressoTopicoRepository progressoRepo;
    private final AnotacaoRepository anotacaoRepo;
    private final CargoProRepository cargoRepo;
    private final ObjectMapper mapper;

    // Mapa de sessão em memória: topicoId -> {questaoId -> {resposta, acertou}}
    // Em produção, use Redis ou persista em tabela separada.
    private final Map<Long, Map<Long, Boolean>> sessaoRespostas = new HashMap<>();

    public LeiSecaService(MateriaRepository materiaRepo,
            TopicoRepository topicoRepo,
            ArtigoLeiRepository artigoRepo,
            QuestaoLeiRepository questaoLeiRepo,
            ProgressoTopicoRepository progressoRepo,
            AnotacaoRepository anotacaoRepo,
            CargoProRepository cargoRepo,
            ObjectMapper mapper) {
        this.materiaRepo = materiaRepo;
        this.topicoRepo = topicoRepo;
        this.artigoRepo = artigoRepo;
        this.questaoLeiRepo = questaoLeiRepo;
        this.progressoRepo = progressoRepo;
        this.anotacaoRepo = anotacaoRepo;
        this.cargoRepo = cargoRepo;
        this.mapper = mapper;
    }

    /* ── Listar Matérias por Cargo ──────────────────────────────────── */
    public List<MateriaResponse> listarMaterias(Long cargoId) {
        List<Materia> materias = materiaRepo.findByCargoIdOrGeral(cargoId);
        return materias.stream().map(this::toMateriaResponse).toList();
    }

    private MateriaResponse toMateriaResponse(Materia m) {
        List<Topico> topicos = topicoRepo.findByMateriaIdAtivos(m.id());
        List<TopicoResumoResponse> resumos = topicos.stream()
                .map(this::toTopicoResumo).toList();

        long concluidos = resumos.stream()
                .filter(t -> "CONCLUIDO".equals(t.status())).count();
        double pct = topicos.isEmpty() ? 0
                : concluidos * 100.0 / topicos.size();

        return new MateriaResponse(
                m.id(), m.nome(), m.descricao(), m.fonteLegal(),
                topicos.size(), (int) concluidos, pct, resumos);
    }

    private TopicoResumoResponse toTopicoResumo(Topico t) {
        int total = questaoLeiRepo.countByTopicoId(t.id());
        ProgressoTopico prog = progressoRepo.findByTopicoId(t.id()).orElse(null);

        int acertos = prog != null ? prog.acertos() : 0;
        int erros = prog != null ? prog.erros() : 0;
        String status = prog != null ? prog.status() : "PENDENTE";
        double aprov = (acertos + erros) > 0
                ? acertos * 100.0 / (acertos + erros)
                : 0;

        return new TopicoResumoResponse(
                t.id(), t.nome(), t.artigoBase(),
                status, total, acertos, erros, aprov);
    }

    /* ── Detalhe de Tópico (artigos + questões) ─────────────────────── */
    public TopicoResponse detalheTopico(Long topicoId) {
        Topico t = topicoRepo.findById(topicoId)
                .orElseThrow(() -> new RuntimeException("Tópico não encontrado"));

        List<ArtigoLei> artigos = artigoRepo.findByTopicoId(topicoId);
        List<QuestaoLei> questoes = questaoLeiRepo.findByTopicoId(topicoId);

        ProgressoTopico prog = progressoRepo.findByTopicoId(topicoId).orElse(null);
        String status = prog != null ? prog.status() : "PENDENTE";

        Map<Long, Boolean> respostas = sessaoRespostas
                .getOrDefault(topicoId, new HashMap<>());

        List<ArtigoResponse> artigosResp = artigos.stream()
                .map(a -> new ArtigoResponse(
                        a.id(), a.identificador(), a.texto(), a.destaque()))
                .toList();

        // Questões: gabarito/comentário só expostos se já respondidas
        List<QuestaoLeiResponse> qResp = questoes.stream()
                .map(q -> {
                    boolean respondida = respostas.containsKey(q.id());
                    Boolean acertou = respondida
                            ? respostas.get(q.id())
                            : null;
                    return new QuestaoLeiResponse(
                            q.id(), q.enunciado(),
                            respondida ? q.gabarito() : null,
                            respondida ? q.comentario() : null,
                            respondida ? q.resumoEstudo() : null,
                            respondida ? q.tipoPegadinha() : null,
                            q.artigoRef(), q.nivel(),
                            q.anoProva(), q.orgaoProva(),
                            q.recorrencia(), respondida, acertou);
                }).toList();

        int acertos = prog != null ? prog.acertos() : 0;
        int erros = prog != null ? prog.erros() : 0;

        return new TopicoResponse(
                t.id(), t.nome(), t.artigoBase(), t.resumo(),
                status, acertos, erros, artigosResp, qResp);
    }

    /* ── Responder Questão ──────────────────────────────────────────── */
    @Transactional
    public QuestaoLeiResponse responder(ResponderLeiRequest req) {
        QuestaoLei q = questaoLeiRepo.findById(req.questaoId())
                .orElseThrow(() -> new RuntimeException("Questão não encontrada"));

        boolean acertou = q.gabarito().equals(req.resposta());

        // Registra na sessão em memória
        sessaoRespostas
                .computeIfAbsent(q.topicoId(), k -> new HashMap<>())
                .put(q.id(), acertou);

        // Atualiza progresso persistido
        ProgressoTopico atual = progressoRepo.findByTopicoId(q.topicoId()).orElse(null);
        int acertos = (atual != null ? atual.acertos() : 0) + (acertou ? 1 : 0);
        int erros = (atual != null ? atual.erros() : 0) + (acertou ? 0 : 1);

        // Status: concluído se respondeu todas as questões do tópico
        int total = questaoLeiRepo.countByTopicoId(q.topicoId());
        int respondidas = sessaoRespostas
                .getOrDefault(q.topicoId(), new HashMap<>()).size();
        String status = respondidas >= total ? "CONCLUIDO" : "EM_ESTUDO";

        progressoRepo.upsert(q.topicoId(), status, acertos, erros);

        return new QuestaoLeiResponse(
                q.id(), q.enunciado(), q.gabarito(),
                q.comentario(), q.resumoEstudo(), q.tipoPegadinha(),
                q.artigoRef(), q.nivel(), q.anoProva(), q.orgaoProva(),
                q.recorrencia(), true, acertou);
    }

    /* ── Resetar progresso de tópico ────────────────────────────────── */
    @Transactional
    public void resetarTopico(Long topicoId) {
        progressoRepo.resetarPorTopico(topicoId);
        sessaoRespostas.remove(topicoId);
    }

    /* ── Anotações ──────────────────────────────────────────────────── */
    @Transactional
    public Anotacao salvarAnotacao(Long topicoId, Long artigoId, String conteudo) {
        return anotacaoRepo.save(
                new Anotacao(null, topicoId, artigoId, conteudo, LocalDateTime.now()));
    }

    public List<Anotacao> listarAnotacoes(Long topicoId) {
        return anotacaoRepo.findByTopicoId(topicoId);
    }

    /* ── Importação JSON ────────────────────────────────────────────── */
    @Transactional
    public ImportacaoLeiResponse importar(String jsonBruto) {
        int materiasNovas = 0, topicosNovos = 0, artigosNovos = 0, questoesOk = 0, ignoradas = 0;
        List<String> erros = new ArrayList<>();

        JsonNode arr;
        try {
            String limpo = jsonBruto.strip()
                    .replaceAll("(?s)^```[a-zA-Z]*\\n?", "").replaceAll("```$", "").strip();
            arr = mapper.readTree(limpo);
        } catch (Exception e) {
            return new ImportacaoLeiResponse(0, 0, 0, 0, 0, 0,
                    List.of("JSON inválido: " + e.getMessage()));
        }

        if (!arr.isArray())
            return new ImportacaoLeiResponse(0, 0, 0, 0, 0, 0,
                    List.of("Esperado array JSON"));

        int total = arr.size();

        for (int i = 0; i < total; i++) {
            JsonNode node = arr.get(i);
            String ctx = "Item #" + (i + 1);
            try {
                // ── Campos obrigatórios ──
                String nomeMateria = txt(node, "materia");
                String nomeTopico = txt(node, "topico");
                String enunciado = txt(node, "enunciado");
                String comentario = txt(node, "comentario");

                if (blank(nomeMateria)) {
                    erros.add(ctx + ": 'materia' obrigatório");
                    ignoradas++;
                    continue;
                }
                if (blank(nomeTopico)) {
                    erros.add(ctx + ": 'topico' obrigatório");
                    ignoradas++;
                    continue;
                }
                if (blank(enunciado)) {
                    erros.add(ctx + ": 'enunciado' obrigatório");
                    ignoradas++;
                    continue;
                }
                if (blank(comentario)) {
                    erros.add(ctx + ": 'comentario' obrigatório");
                    ignoradas++;
                    continue;
                }

                Boolean gabarito = resolverGabarito(node);
                if (gabarito == null) {
                    erros.add(ctx + ": 'gabarito' inválido");
                    ignoradas++;
                    continue;
                }

                // ── Campos opcionais ──
                String fonteLegal = txt(node, "fonte_legal");
                String descMateria = txt(node, "descricao_materia");
                String artigoBase = txt(node, "artigo_base");
                String resumoTopico = txt(node, "resumo_topico");
                String artigoIdent = txt(node, "artigo_identificador");
                String artigoTexto = txt(node, "artigo_texto");
                String resumoEstudo = txt(node, "resumo_estudo");
                String tipoPeg = txt(node, "tipo_pegadinha");
                String artigoRef = txt(node, "artigo_ref");
                String nivelRaw = txt(node, "nivel");
                String orgaoProva = txt(node, "orgao_prova");
                String cargoNome = txt(node, "cargo");
                Integer anoProva = node.has("ano_prova") && !node.get("ano_prova").isNull()
                        ? node.get("ano_prova").asInt()
                        : null;
                Integer recorrencia = node.has("recorrencia") && !node.get("recorrencia").isNull()
                        ? node.get("recorrencia").asInt()
                        : 1;

                String nivel = resolverNivel(nivelRaw);

                // ── Cargo ──
                Long cargoId = null;
                if (!blank(cargoNome)) {
                    String cn = cargoNome;
                    cargoId = cargoRepo.findByNomeIgnoreCase(cn)
                            .map(CargoPro::id).orElse(null);
                }

                // ── Matéria (cria se não existir) ──
                Materia materia = materiaRepo.findByNomeIgnoreCase(nomeMateria)
                        .orElse(null);
                if (materia == null) {
                    materia = materiaRepo.save(new Materia(
                            null, cargoId, nomeMateria,
                            descMateria, fonteLegal, 0, true, LocalDateTime.now()));
                    materiasNovas++;
                }

                // ── Tópico (cria se não existir) ──
                Topico topico = topicoRepo
                        .findByMateriaIdAndNomeIgnoreCase(materia.id(), nomeTopico)
                        .orElse(null);
                if (topico == null) {
                    topico = topicoRepo.save(new Topico(
                            null, materia.id(), nomeTopico,
                            artigoBase, resumoTopico, 0, true));
                    topicosNovos++;
                }

                // ── Artigo (cria se texto informado) ──
                Long artigoId = null;
                if (!blank(artigoIdent) && !blank(artigoTexto)) {
                    ArtigoLei artigo = artigoRepo.save(new ArtigoLei(
                            null, topico.id(), artigoIdent, artigoTexto,
                            false, 0));
                    artigoId = artigo.id();
                    artigosNovos++;
                }

                // ── Questão ──
                questaoLeiRepo.save(new QuestaoLei(
                        null, topico.id(), artigoId,
                        enunciado, gabarito, comentario, resumoEstudo,
                        tipoPeg, artigoRef, nivel,
                        anoProva, orgaoProva,
                        recorrencia != null ? recorrencia : 1,
                        true, LocalDateTime.now()));
                questoesOk++;

            } catch (Exception e) {
                erros.add(ctx + ": " + e.getMessage());
                ignoradas++;
            }
        }

        return new ImportacaoLeiResponse(
                total, materiasNovas, topicosNovos,
                artigosNovos, questoesOk, ignoradas, erros);
    }

    /* ── Stats ──────────────────────────────────────────────────────── */
    public Map<String, Object> stats(Long cargoId) {
        List<Materia> materias = materiaRepo.findByCargoIdOrGeral(cargoId);
        long totalTopicos = materias.stream()
                .mapToLong(m -> topicoRepo.findByMateriaIdAtivos(m.id()).size()).sum();
        long concluidos = materias.stream()
                .flatMap(m -> topicoRepo.findByMateriaIdAtivos(m.id()).stream())
                .filter(t -> progressoRepo.findByTopicoId(t.id())
                        .map(p -> "CONCLUIDO".equals(p.status())).orElse(false))
                .count();
        return Map.of(
                "materias", materias.size(),
                "topicos", totalTopicos,
                "concluidos", concluidos,
                "questoes", questaoLeiRepo.countAtivas(),
                "progresso", totalTopicos > 0
                        ? Math.round(concluidos * 100.0 / totalTopicos)
                        : 0);
    }

    /* ── Helpers ────────────────────────────────────────────────────── */
    private Boolean resolverGabarito(JsonNode n) {
        if (!n.has("gabarito") || n.get("gabarito").isNull())
            return null;
        JsonNode g = n.get("gabarito");
        if (g.isBoolean())
            return g.asBoolean();
        return switch (g.asText("").toLowerCase().strip()) {
            case "certo", "true", "c", "sim" -> true;
            case "errado", "false", "e", "nao", "não" -> false;
            default -> null;
        };
    }

    private String resolverNivel(String raw) {
        if (raw == null)
            return "MEDIO";
        return switch (raw.toLowerCase().strip()
                .replace("á", "a").replace("é", "e")
                .replace("í", "i").replace("ó", "o")
                .replace("ú", "u").replace("ã", "a")) {
            case "facil", "easy", "f" -> "FACIL";
            case "dificil", "hard", "d" -> "DIFICIL";
            default -> "MEDIO";
        };
    }

    private String txt(JsonNode n, String c) {
        if (!n.has(c) || n.get(c).isNull())
            return null;
        String v = n.get(c).asText("").strip();
        return v.isEmpty() ? null : v;
    }

    private boolean blank(String s) {
        return s == null || s.isBlank();
    }
}