package br.com.fiap.transitoclaro.web;

import br.com.fiap.transitoclaro.service.StatusService;
import br.com.fiap.transitoclaro.web.Dtos.StatusLinhaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Status das linhas")
public class StatusController {

    private final StatusService service;

    public StatusController(StatusService service) {
        this.service = service;
    }

    @GetMapping("/status")
    @Operation(summary = "Status atual de todas as linhas (as mais problematicas primeiro)")
    public List<StatusLinhaResponse> todas(@RequestParam(name = "janela_minutos", defaultValue = "60") int janela) {
        return service.obterTodas(janela);
    }

    @GetMapping("/status/{linhaId}")
    @Operation(summary = "Status atual de uma linha, calculado a partir dos relatos recentes")
    public StatusLinhaResponse uma(@PathVariable Long linhaId,
                                   @RequestParam(name = "janela_minutos", defaultValue = "60") int janela) {
        return service.obter(linhaId, janela);
    }

    @GetMapping("/saude")
    @Operation(summary = "Health check")
    public Map<String, String> saude() {
        return Map.of("status", "ok");
    }
}
