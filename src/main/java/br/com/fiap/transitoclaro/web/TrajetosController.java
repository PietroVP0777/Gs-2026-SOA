package br.com.fiap.transitoclaro.web;

import br.com.fiap.transitoclaro.service.AlertaService;
import br.com.fiap.transitoclaro.service.TrajetoService;
import br.com.fiap.transitoclaro.web.Dtos.AlertaResponse;
import br.com.fiap.transitoclaro.web.Dtos.TrajetoRequest;
import br.com.fiap.transitoclaro.web.Dtos.TrajetoResponse;
import br.com.fiap.transitoclaro.web.Dtos.VerificacaoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/trajetos")
@Tag(name = "Trajetos e alertas")
public class TrajetosController {

    private final TrajetoService trajetoService;
    private final AlertaService alertaService;

    public TrajetosController(TrajetoService trajetoService, AlertaService alertaService) {
        this.trajetoService = trajetoService;
        this.alertaService = alertaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra um trajeto com 1 a 6 linhas")
    public TrajetoResponse criar(@Valid @RequestBody TrajetoRequest dados) {
        return trajetoService.criar(dados);
    }

    @GetMapping
    @Operation(summary = "Lista trajetos")
    public List<TrajetoResponse> listar(
            @RequestParam(name = "apenas_ativos", defaultValue = "false") boolean apenasAtivos) {
        return trajetoService.listar(apenasAtivos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtem um trajeto")
    public TrajetoResponse obter(@PathVariable Long id) {
        return trajetoService.obter(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um trajeto")
    public TrajetoResponse atualizar(@PathVariable Long id, @Valid @RequestBody TrajetoRequest dados) {
        return trajetoService.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove um trajeto")
    public void remover(@PathVariable Long id) {
        trajetoService.remover(id);
    }

    @PostMapping("/{id}/verificar")
    @Operation(summary = "Verifica o trajeto agora",
            description = "Calcula o status de cada linha do trajeto a partir dos relatos recentes, "
                    + "escolhe o pior e salva o alerta no historico.")
    public VerificacaoResponse verificar(@PathVariable Long id) {
        return alertaService.verificar(id);
    }

    @GetMapping("/{id}/alertas")
    @Operation(summary = "Historico de alertas do trajeto")
    public List<AlertaResponse> historico(@PathVariable Long id, @RequestParam(defaultValue = "20") int limite) {
        return alertaService.historico(id, limite);
    }
}
