package br.com.fiap.transitoclaro;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class RelatoApiTest extends BaseApiTest {

    private String corpo(long linhaId, String tipo, String extras) {
        return "{\"linha_id\":" + linhaId + ",\"tipo\":\"" + tipo + "\"" + extras + "}";
    }

    @Test
    void criarRelatoDeAtrasoRetorna201() throws Exception {
        mockMvc.perform(post("/relatos").contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(idLinha(CORAL), "ATRASO", ",\"minutos_atraso\":12,\"descricao\":\"Trem parado\"")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.linha_nome").value(CORAL))
                .andExpect(jsonPath("$.minutos_atraso").value(12));
    }

    @Test
    void criarAtrasoSemMinutosRetorna400() throws Exception {
        mockMvc.perform(post("/relatos").contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(idLinha(CORAL), "ATRASO", "")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void criarAtrasoComMinutosForaDoLimiteRetorna422() throws Exception {
        mockMvc.perform(post("/relatos").contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(idLinha(CORAL), "ATRASO", ",\"minutos_atraso\":500")))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void criarLotacaoSemNivelRetorna400() throws Exception {
        mockMvc.perform(post("/relatos").contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(idLinha(AZUL), "LOTACAO", "")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void criarRelatoEmLinhaInexistenteRetorna404() throws Exception {
        mockMvc.perform(post("/relatos").contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(99999, "FALHA_OPERACIONAL", "")))
                .andExpect(status().isNotFound());
    }

    @Test
    void listarFiltraPorLinhaEOrdenaDoMaisRecente() throws Exception {
        postarRelato(AZUL, "ATRASO", null, 5);
        postarRelato(AZUL, "ATRASO", null, 9);
        postarRelato(VERMELHA, "ATRASO", null, 3);

        mockMvc.perform(get("/relatos").param("linha_id", String.valueOf(idLinha(AZUL))))
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.itens", hasSize(2)))
                .andExpect(jsonPath("$.itens[0].minutos_atraso").value(9));
    }

    @Test
    void listarFiltraPorTipo() throws Exception {
        postarRelato(AZUL, "ATRASO", null, 5);
        postarRelato(AZUL, "LOTACAO", "CHEIO", null);

        mockMvc.perform(get("/relatos").param("tipo", "LOTACAO"))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.itens[0].nivel_lotacao").value("CHEIO"));
    }

    @Test
    void estatisticasRankeiamLinhasPorAtrasos() throws Exception {
        postarRelato(VERMELHA, "ATRASO", null, 10);
        postarRelato(VERMELHA, "ATRASO", null, 20);
        postarRelato(AZUL, "ATRASO", null, 5);
        postarRelato(AZUL, "LOTACAO", "LOTADO", null);

        mockMvc.perform(get("/relatos/estatisticas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total_relatos").value(4))
                .andExpect(jsonPath("$.por_tipo.ATRASO").value(3))
                .andExpect(jsonPath("$.linhas_com_mais_atrasos[0].linha").value(VERMELHA))
                .andExpect(jsonPath("$.linhas_com_mais_atrasos[0].atraso_medio_minutos").value(15.0));
    }

    @Test
    void removerRelatoRetorna204EDepois404() throws Exception {
        long id = postarRelato(AZUL, "FALHA_OPERACIONAL", null, null);
        mockMvc.perform(delete("/relatos/" + id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/relatos/" + id)).andExpect(status().isNotFound());
    }
}
