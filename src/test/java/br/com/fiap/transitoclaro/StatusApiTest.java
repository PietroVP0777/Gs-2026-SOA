package br.com.fiap.transitoclaro;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.fiap.transitoclaro.domain.Relato;
import br.com.fiap.transitoclaro.domain.TipoOcorrencia;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class StatusApiTest extends BaseApiTest {

    @Test
    void linhaSemRelatosEhNormal() throws Exception {
        mockMvc.perform(get("/status/" + idLinha(AZUL)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NORMAL"))
                .andExpect(jsonPath("$.total_relatos").value(0));
    }

    @Test
    void atrasoGrandeDeixaALinhaCritica() throws Exception {
        postarRelato(CORAL, "ATRASO", null, 25);

        mockMvc.perform(get("/status/" + idLinha(CORAL)))
                .andExpect(jsonPath("$.status").value("CRITICO"))
                .andExpect(jsonPath("$.atraso_medio_minutos").value(25.0))
                .andExpect(jsonPath("$.total_relatos").value(1));
    }

    @Test
    void lotacaoPredominanteEhOMaisFrequente() throws Exception {
        postarRelato(AZUL, "LOTACAO", "CHEIO", null);
        postarRelato(AZUL, "LOTACAO", "CHEIO", null);
        postarRelato(AZUL, "LOTACAO", "VAZIO", null);

        mockMvc.perform(get("/status/" + idLinha(AZUL)))
                .andExpect(jsonPath("$.lotacao_predominante").value("CHEIO"))
                .andExpect(jsonPath("$.status").value("MODERADO"));
    }

    @Test
    void relatoForaDaJanelaEhIgnorado() throws Exception {
        var linha = linhaRepository.findByNome(CORAL).orElseThrow();
        var antigo = Relato.criar(linha, TipoOcorrencia.ATRASO, null, 30, null,
                LocalDateTime.now(ZoneOffset.UTC).minusHours(3));
        relatoRepository.save(antigo);

        mockMvc.perform(get("/status/" + linha.getId()).param("janela_minutos", "60"))
                .andExpect(jsonPath("$.status").value("NORMAL"))
                .andExpect(jsonPath("$.total_relatos").value(0));
    }

    @Test
    void linhaInexistenteRetorna404() throws Exception {
        mockMvc.perform(get("/status/99999")).andExpect(status().isNotFound());
    }

    @Test
    void todasAsLinhasVemComAMaisCriticaPrimeiro() throws Exception {
        postarRelato(VERMELHA, "ATRASO", null, 30);

        mockMvc.perform(get("/status"))
                .andExpect(jsonPath("$", hasSize(6)))
                .andExpect(jsonPath("$[0].linha").value(VERMELHA))
                .andExpect(jsonPath("$[0].status").value("CRITICO"));
    }
}
