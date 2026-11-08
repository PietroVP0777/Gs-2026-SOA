package br.com.fiap.transitoclaro.service;

import br.com.fiap.transitoclaro.domain.Linha;
import br.com.fiap.transitoclaro.domain.LinhaRepository;
import br.com.fiap.transitoclaro.domain.NaoEncontradoException;
import br.com.fiap.transitoclaro.domain.Relato;
import br.com.fiap.transitoclaro.domain.RelatoRepository;
import br.com.fiap.transitoclaro.domain.TipoOcorrencia;
import br.com.fiap.transitoclaro.web.Dtos.EstatisticasResponse;
import br.com.fiap.transitoclaro.web.Dtos.LinhaAtrasoResponse;
import br.com.fiap.transitoclaro.web.Dtos.PaginaResponse;
import br.com.fiap.transitoclaro.web.Dtos.RelatoRequest;
import br.com.fiap.transitoclaro.web.Dtos.RelatoResponse;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RelatoService {

    private final RelatoRepository relatos;
    private final LinhaRepository linhas;
    private final Clock clock;

    public RelatoService(RelatoRepository relatos, LinhaRepository linhas, Clock clock) {
        this.relatos = relatos;
        this.linhas = linhas;
        this.clock = clock;
    }

    @Transactional
    public RelatoResponse criar(RelatoRequest dados) {
        Linha linha = linhas.findById(dados.linhaId())
                .orElseThrow(() -> new NaoEncontradoException("Linha " + dados.linhaId() + " nao encontrada."));

        Relato relato = Relato.criar(linha, dados.tipo(), dados.nivelLotacao(), dados.minutosAtraso(),
                dados.descricao(), LocalDateTime.now(clock));
        return RelatoResponse.de(relatos.save(relato));
    }

    @Transactional(readOnly = true)
    public PaginaResponse<RelatoResponse> listar(Long linhaId, TipoOcorrencia tipo, int pagina, int tamanho) {
        int p = Math.max(pagina, 1);
        int t = Math.min(Math.max(tamanho, 1), 100);
        var paginacao = PageRequest.of(p - 1, t, Sort.by(Sort.Direction.DESC, "criadoEm", "id"));

        Page<Relato> resultado;
        if (linhaId != null && tipo != null) {
            resultado = relatos.findByLinhaIdAndTipo(linhaId, tipo, paginacao);
        } else if (linhaId != null) {
            resultado = relatos.findByLinhaId(linhaId, paginacao);
        } else if (tipo != null) {
            resultado = relatos.findByTipo(tipo, paginacao);
        } else {
            resultado = relatos.findAll(paginacao);
        }

        return new PaginaResponse<>(
                resultado.getContent().stream().map(RelatoResponse::de).toList(), p, t, resultado.getTotalElements());
    }

    @Transactional(readOnly = true)
    public RelatoResponse obter(Long id) {
        return RelatoResponse.de(relatos.findById(id)
                .orElseThrow(() -> new NaoEncontradoException("Relato " + id + " nao encontrado.")));
    }

    @Transactional
    public void remover(Long id) {
        relatos.delete(relatos.findById(id)
                .orElseThrow(() -> new NaoEncontradoException("Relato " + id + " nao encontrado.")));
    }

    /** Indicador de impacto: volume de relatos e linhas mais afetadas por atraso no periodo. */
    @Transactional(readOnly = true)
    public EstatisticasResponse estatisticas(int horas) {
        int h = Math.min(Math.max(horas, 1), 24 * 30);
        List<Relato> periodo = relatos.findByCriadoEmGreaterThanEqual(LocalDateTime.now(clock).minusHours(h));

        Map<String, Long> porTipo = periodo.stream()
                .collect(Collectors.groupingBy(r -> r.getTipo().name(), Collectors.counting()));

        List<LinhaAtrasoResponse> ranking = periodo.stream()
                .filter(r -> r.getTipo() == TipoOcorrencia.ATRASO && r.getMinutosAtraso() != null)
                .collect(Collectors.groupingBy(r -> r.getLinha()))
                .entrySet().stream()
                .map(e -> new LinhaAtrasoResponse(
                        e.getKey().getId(),
                        e.getKey().getNome(),
                        e.getValue().size(),
                        arredondar(e.getValue().stream().mapToInt(Relato::getMinutosAtraso).average().orElse(0))))
                .sorted(Comparator.comparingLong(LinhaAtrasoResponse::totalAtrasos).reversed()
                        .thenComparing(Comparator.comparingDouble(LinhaAtrasoResponse::atrasoMedioMinutos).reversed()))
                .limit(5)
                .toList();

        return new EstatisticasResponse(h, periodo.size(), porTipo, ranking);
    }

    private static double arredondar(double valor) {
        return Math.round(valor * 10.0) / 10.0;
    }
}
