package com.cebraspe.service;

import com.cebraspe.record.*;
import com.cebraspe.record.request.CriarSessaoRequest;
import com.cebraspe.record.request.ResponderRequest;
import com.cebraspe.record.response.*;
import com.cebraspe.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ProvaService {

    private final QuestaoProRepository questaoRepo;
    private final SessaoProvaRepository sessaoRepo;
    private final RespostaProRepository respostaRepo;
    private final TemaRepository temaRepo;
    private final SubtemaRepository subtemaRepo;
    private final ObjectMapper mapper;
    private final EstatisticaQueryRepository statsRepo;

    @Autowired
    private EstatisticaService estatisticaService;

    // Construtor atualizado:
    public ProvaService(QuestaoProRepository questaoRepo,
            SessaoProvaRepository sessaoRepo,
            RespostaProRepository respostaRepo,
            TemaRepository temaRepo,
            SubtemaRepository subtemaRepo,
            EstatisticaQueryRepository statsRepo,
            EstatisticaService estatisticaService,
            ObjectMapper mapper) {
        this.questaoRepo = questaoRepo;
        this.sessaoRepo = sessaoRepo;
        this.respostaRepo = respostaRepo;
        this.temaRepo = temaRepo;
        this.subtemaRepo = subtemaRepo;
        this.statsRepo = statsRepo;
        this.estatisticaService = estatisticaService;
        this.mapper = mapper;
    }

    @Transactional
    public SessaoIniciadaResponse criarSessao(CriarSessaoRequest req) throws Exception {
        List<QuestaoProva> pool = montarPool(req);

        if (pool.isEmpty())
            throw new RuntimeException("Nenhuma questão disponível com esses filtros.");

        // Não repetir
        pool = pool.stream().distinct().limit(req.totalQuestoes()).toList();

        String configJson = mapper.writeValueAsString(req);
        SessaoProva sessao = sessaoRepo.save(new SessaoProva(
                null, req.cargoId(), configJson, "EM_ANDAMENTO",
                pool.size(), 0, 0, null, LocalDateTime.now(), null));

        List<QuestaoProvaResponse> questoes = new ArrayList<>();
        for (int i = 0; i < pool.size(); i++) {
            QuestaoProva q = pool.get(i);
            String temaNome = temaRepo.findById(q.temaId()).map(Tema::nome).orElse("");
            String subtemaNome = subtemaRepo.findById(q.subtemaId()).map(Subtema::nome).orElse("");
            questoes.add(new QuestaoProvaResponse(
                    q.id(), i + 1, temaNome, subtemaNome,
                    q.enunciado(), q.nivelDificuldade(),
                    null, null, null, null, null, null));
        }

        return new SessaoIniciadaResponse(sessao.id(), pool.size(), questoes);
    }

    // montarPool reescrito — sem mais queries problemáticas no repositório:
    private List<QuestaoProva> montarPool(CriarSessaoRequest req) {
        int total = req.totalQuestoes();
        String grupo = req.grupo() != null ? req.grupo().toUpperCase() : "EDITAL";

        List<Long> ids;

        if (Boolean.TRUE.equals(req.focarDificuldades())) {
            ids = statsRepo.idsQuestoesDificuldades(
                    grupo, req.cargoId(), total);
        } else if (Boolean.TRUE.equals(req.aleatorio())) {
            ids = statsRepo.idsQuestoesAleatorio(
                    grupo, req.cargoId(), total);
        } else {
            int pF = req.pctFacil() != null ? req.pctFacil() : 33;
            int pM = req.pctMedio() != null ? req.pctMedio() : 34;
            int pD = req.pctDificil() != null ? req.pctDificil() : 33;
            int qF = (int) Math.round(total * pF / 100.0);
            int qM = (int) Math.round(total * pM / 100.0);
            int qD = Math.max(0, total - qF - qM);

            ids = new ArrayList<>();
            ids.addAll(statsRepo.idsQuestoesPorNivel(grupo, "FACIL", req.cargoId(), qF));
            ids.addAll(statsRepo.idsQuestoesPorNivel(grupo, "MEDIO", req.cargoId(), qM));
            ids.addAll(statsRepo.idsQuestoesPorNivel(grupo, "DIFICIL", req.cargoId(), qD));
        }

        if (ids.isEmpty())
            throw new RuntimeException(
                    "Nenhuma questão disponível para o grupo '" + grupo +
                            "'. Tente resetar as questões ou adicionar mais questões.");

        // Carrega as entidades pelos IDs (findAllById é suportado pelo CrudRepository)
        List<QuestaoProva> pool = new ArrayList<>();
        questaoRepo.findAllById(ids).forEach(pool::add);

        // Embaralha no service (não depende de RANDOM() do banco)
        Collections.shuffle(pool);
        return pool;
    }

    @Transactional
    public RespostaProResponse responder(Long sessaoId, ResponderRequest req) {
        QuestaoProva q = questaoRepo.findById(req.questaoId())
                .orElseThrow(() -> new RuntimeException("Questão não encontrada"));

        boolean acertou = q.gabarito().equals(req.resposta());

        respostaRepo.save(new RespostaPro(
                null, sessaoId, q.id(), q.temaId(), q.subtemaId(),
                q.nivelDificuldade(), req.resposta(), acertou,
                req.tempoMs(), LocalDateTime.now()));

        return new RespostaProResponse(
                q.id(), acertou, q.gabarito(),
                q.explicacao(), q.tipoPegadinha(), q.artigoRef());
    }

    // finalizarSessao — usa o service correto:
    @Transactional
    public Map<String, Object> finalizarSessao(Long sessaoId) {
        List<RespostaPro> respostas = respostaRepo.findBySessaoId(sessaoId);
        int ac = (int) respostas.stream().filter(RespostaPro::acertou).count();
        int er = respostas.size() - ac;
        long tempo = respostas.stream().mapToLong(RespostaPro::tempoMs).sum();

        sessaoRepo.finalizar(sessaoId, "CONCLUIDA", ac, er, tempo);

        // Bloqueia questões acertadas usando DTOs corretos
        estatisticaService.marcarCorretasBloqueadas(sessaoId);

        double aprov = respostas.isEmpty() ? 0 : ac * 100.0 / respostas.size();
        return Map.of(
                "sessaoId", sessaoId,
                "acertos", ac,
                "erros", er,
                "total", respostas.size(),
                "aproveitamento", aprov,
                "tempoTotalMs", tempo);
    }
}