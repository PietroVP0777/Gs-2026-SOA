package br.com.fiap.transitoclaro.service;

import br.com.fiap.transitoclaro.domain.ClassificadorStatus;
import br.com.fiap.transitoclaro.domain.Linha;
import br.com.fiap.transitoclaro.domain.LinhaRepository;
import br.com.fiap.transitoclaro.domain.NaoEncontradoException;
import br.com.fiap.transitoclaro.domain.NivelLotacao;
import br.com.fiap.transitoclaro.domain.Regras;
import br.com.fiap.transitoclaro.domain.Relato;
import br.com.fiap.transitoclaro.domain.RelatoRepository;
import br.com.fiap.transitoclaro.domain.StatusLinha;
import br.com.fiap.transitoclaro.domain.TipoOcorrencia;
import br.com.fiap.transitoclaro.web.Dtos.StatusLinhaResponse;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Responde a pergunta principal do passageiro: "como esta minha linha agora?" */
@Service
public class StatusService {

    private final LinhaRepository linhas;
    private final RelatoRepository relatos;
    private final ClassificadorStatus classificador;
    private final Clock clock;

    public StatusService(LinhaRepository linhas, RelatoRepository relatos, ClassificadorStatus classificador,
                         Clock clock) {
        this.linhas = linhas;
        this.relatos = relatos;
        this.classificador = classificador;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public StatusLinhaResponse obter(Long linhaId, int janelaMinutos) {
        int janela = normalizar(janelaMinutos);
        Linha linha = linhas.findById(linhaId)
                .orElseThrow(() -> new NaoEncontradoException("Linha " + linhaId + " nao encontrada."));

        LocalDateTime limite = LocalDateTime.now(clock).minusMinutes(janela);
        List<Relato> recentes = relatos.findByLinhaIdAndCriadoEmGreaterThanEqual(linhaId, limite);
        return montar(linha, recentes, janela);
    }

    @Transactional(readOnly = true)
    public List<StatusLinhaResponse> obterTodas(int janelaMinutos) {
        int janela = normalizar(janelaMinutos);
        LocalDateTime limite = LocalDateTime.now(clock).minusMinutes(janela);

        Map<Long, List<Relato>> porLinha = relatos.findByCriadoEmGreaterThanEqual(limite).stream()
                .collect(Collectors.groupingBy(r -> r.getLinha().getId()));

        return linhas.findAll().stream()
                .map(l -> montar(l, porLinha.getOrDefault(l.getId(), List.of()), janela))
                .sorted(Comparator.comparingInt((StatusLinhaResponse s) -> s.status().getGravidade()).reversed()
                        .thenComparing(Comparator.comparingDouble(StatusLinhaResponse::atrasoMedioMinutos).reversed())
                        .thenComparing(StatusLinhaResponse::linha))
                .toList();
    }

    private StatusLinhaResponse montar(Linha linha, List<Relato> recentes, int janela) {
        StatusLinha status = classificador.classificar(recentes);

        double atrasoMedio = recentes.stream()
                .filter(r -> r.getTipo() == TipoOcorrencia.ATRASO && r.getMinutosAtraso() != null)
                .mapToInt(Relato::getMinutosAtraso)
                .average().orElse(0);

        return new StatusLinhaResponse(
                linha.getId(), linha.getNome(), linha.getTipo(), status,
                Math.round(atrasoMedio * 10.0) / 10.0,
                lotacaoPredominante(recentes),
                recentes.size(), janela,
                Regras.mensagemDaLinha(status, recentes.size(), atrasoMedio));
    }

    /** Nivel de lotacao mais frequente; em empate, o mais lotado. */
    private static NivelLotacao lotacaoPredominante(List<Relato> recentes) {
        Map<NivelLotacao, Long> contagem = recentes.stream()
                .filter(r -> r.getNivelLotacao() != null)
                .collect(Collectors.groupingBy(Relato::getNivelLotacao, Collectors.counting()));

        NivelLotacao predominante = null;
        long melhor = 0;
        for (Map.Entry<NivelLotacao, Long> e : contagem.entrySet()) {
            boolean maisFrequente = e.getValue() > melhor;
            boolean empateMaisLotado = e.getValue() == melhor && predominante != null
                    && e.getKey().compareTo(predominante) > 0;
            if (maisFrequente || empateMaisLotado) {
                predominante = e.getKey();
                melhor = e.getValue();
            }
        }
        return predominante;
    }

    private static int normalizar(int minutos) {
        return Math.min(Math.max(minutos, 5), 720);
    }
}
