package br.com.fiap.transitoclaro.service;

import br.com.fiap.transitoclaro.domain.Alerta;
import br.com.fiap.transitoclaro.domain.AlertaRepository;
import br.com.fiap.transitoclaro.domain.NaoEncontradoException;
import br.com.fiap.transitoclaro.domain.RegraDeNegocioException;
import br.com.fiap.transitoclaro.domain.Regras;
import br.com.fiap.transitoclaro.domain.StatusLinha;
import br.com.fiap.transitoclaro.domain.Trajeto;
import br.com.fiap.transitoclaro.domain.TrajetoRepository;
import br.com.fiap.transitoclaro.web.Dtos.AlertaResponse;
import br.com.fiap.transitoclaro.web.Dtos.StatusLinhaResponse;
import br.com.fiap.transitoclaro.web.Dtos.VerificacaoResponse;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Verifica o status das linhas de um trajeto e transforma o resultado em um alerta para o passageiro. */
@Service
public class AlertaService {

    private static final int JANELA_MINUTOS = 60;

    private final TrajetoRepository trajetos;
    private final AlertaRepository alertas;
    private final StatusService statusService;
    private final Clock clock;

    public AlertaService(TrajetoRepository trajetos, AlertaRepository alertas, StatusService statusService,
                         Clock clock) {
        this.trajetos = trajetos;
        this.alertas = alertas;
        this.statusService = statusService;
        this.clock = clock;
    }

    @Transactional
    public VerificacaoResponse verificar(Long trajetoId) {
        Trajeto trajeto = trajetos.findById(trajetoId)
                .orElseThrow(() -> new NaoEncontradoException("Trajeto " + trajetoId + " nao encontrado."));
        if (!trajeto.isAtivo()) {
            throw new RegraDeNegocioException("Trajeto inativo: ative-o para receber alertas.");
        }

        List<StatusLinhaResponse> statuses = trajeto.getLinhas().stream()
                .map(tl -> statusService.obter(tl.getLinha().getId(), JANELA_MINUTOS))
                .toList();

        StatusLinha pior = Regras.piorStatus(statuses.stream().map(StatusLinhaResponse::status).toList());
        String linhaAfetada = pior == StatusLinha.NORMAL ? null
                : statuses.stream().filter(s -> s.status() == pior).map(StatusLinhaResponse::linha)
                        .findFirst().orElse(null);

        Alerta alerta = alertas.save(new Alerta(trajeto, pior, Regras.recomendar(pior, linhaAfetada),
                linhaAfetada, LocalDateTime.now(clock)));

        return new VerificacaoResponse(AlertaResponse.de(alerta), statuses);
    }

    @Transactional(readOnly = true)
    public List<AlertaResponse> historico(Long trajetoId, int limite) {
        if (!trajetos.existsById(trajetoId)) {
            throw new NaoEncontradoException("Trajeto " + trajetoId + " nao encontrado.");
        }
        int tamanho = Math.min(Math.max(limite, 1), 100);
        return alertas.findByTrajetoIdOrderByCriadoEmDescIdDesc(trajetoId, PageRequest.of(0, tamanho))
                .stream().map(AlertaResponse::de).toList();
    }
}
