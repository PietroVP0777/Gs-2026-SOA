package br.com.fiap.transitoclaro.web;

import br.com.fiap.transitoclaro.domain.TipoOcorrencia;
import br.com.fiap.transitoclaro.service.RelatoService;
import br.com.fiap.transitoclaro.web.Dtos.EstatisticasResponse;
import br.com.fiap.transitoclaro.web.Dtos.PaginaResponse;
import br.com.fiap.transitoclaro.web.Dtos.RelatoRequest;
import br.com.fiap.transitoclaro.web.Dtos.RelatoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/relatos")
@Tag(name = "Relatos dos passageiros")
public class RelatosController {

    private final RelatoService service;

    public RelatosController(RelatoService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Envia um relato anonimo (atraso, lotacao ou falha operacional)")
    public RelatoResponse criar(@Valid @RequestBody RelatoRequest dados) {
        return service.criar(dados);
    }

    @GetMapping
    @Operation(summary = "Lista relatos, do mais recente para o mais antigo (paginado)")
    public PaginaResponse<RelatoResponse> listar(
            @RequestParam(name = "linha_id", required = false) Long linhaId,
            @RequestParam(required = false) TipoOcorrencia tipo,
            @RequestParam(defaultValue = "1") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return service.listar(linhaId, tipo, pagina, tamanho);
    }

    @GetMapping("/estatisticas")
    @Operation(summary = "Indicador de impacto: total de relatos e linhas com mais atrasos no periodo")
    public EstatisticasResponse estatisticas(@RequestParam(defaultValue = "24") int horas) {
        return service.estatisticas(horas);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtem um relato")
    public RelatoResponse obter(@PathVariable Long id) {
        return service.obter(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove um relato")
    public void remover(@PathVariable Long id) {
        service.remover(id);
    }
}
