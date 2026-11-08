package br.com.fiap.transitoclaro;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class LinhaApiTest extends BaseApiTest {

    private static final String NOVA = """
            {"nome":"Linha 15-Prata","tipo":"METRO","operadora":"Metro SP","cor":"Prata"}
            """;

    @Test
    void listarTodasTrazAsLinhasIniciais() throws Exception {
        mockMvc.perform(get("/linhas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(6)));
    }

    @Test
    void listarFiltrandoPorTipo() throws Exception {
        mockMvc.perform(get("/linhas").param("tipo", "TREM"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].tipo").value("TREM"));
    }

    @Test
    void criarLinhaRetorna201() throws Exception {
        mockMvc.perform(post("/linhas").contentType(MediaType.APPLICATION_JSON).content(NOVA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nome").value("Linha 15-Prata"));
    }

    @Test
    void criarLinhaComNomeDuplicadoRetorna400() throws Exception {
        String duplicada = """
                {"nome":"Linha 1-Azul","tipo":"METRO","operadora":"Qualquer"}
                """;
        mockMvc.perform(post("/linhas").contentType(MediaType.APPLICATION_JSON).content(duplicada))
                .andExpect(status().isBadRequest());
    }

    @Test
    void criarLinhaInvalidaRetorna422() throws Exception {
        mockMvc.perform(post("/linhas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"\",\"operadora\":\"X\"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void atualizarLinha() throws Exception {
        String criada = mockMvc.perform(post("/linhas").contentType(MediaType.APPLICATION_JSON).content(NOVA))
                .andReturn().getResponse().getContentAsString();
        int id = JsonPath.read(criada, "$.id");

        mockMvc.perform(put("/linhas/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Linha 15-Prata (Monotrilho)\",\"tipo\":\"METRO\",\"operadora\":\"Metro SP\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Linha 15-Prata (Monotrilho)"));
    }

    @Test
    void obterInexistenteRetorna404() throws Exception {
        mockMvc.perform(get("/linhas/99999")).andExpect(status().isNotFound());
    }

    @Test
    void removerLinhaSemUsoRetorna204() throws Exception {
        String criada = mockMvc.perform(post("/linhas").contentType(MediaType.APPLICATION_JSON).content(NOVA))
                .andReturn().getResponse().getContentAsString();
        int id = JsonPath.read(criada, "$.id");

        mockMvc.perform(delete("/linhas/" + id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/linhas/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void removerLinhaEmUsoEmTrajetoRetorna400() throws Exception {
        criarTrajeto();
        mockMvc.perform(delete("/linhas/" + idLinha(CORAL))).andExpect(status().isBadRequest());
    }
}
