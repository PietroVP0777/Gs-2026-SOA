package br.com.fiap.transitoclaro;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class TrajetoApiTest extends BaseApiTest {

    private String ids(String... linhas) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < linhas.length; i++) {
            sb.append(i > 0 ? "," : "").append(idLinha(linhas[i]));
        }
        return sb.append("]").toString();
    }

    // ---------- CRUD ----------
    @Test
    void criarTrajetoRetorna201() throws Exception {
        mockMvc.perform(post("/trajetos").contentType(MediaType.APPLICATION_JSON)
                        .content(payloadTrajeto("Ida", true, ids(CORAL, VERMELHA))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.horario_saida").value("07:30:00"))
                .andExpect(jsonPath("$.linhas[0].nome_linha").value(CORAL))
                .andExpect(jsonPath("$.linhas[0].ordem").value(1))
                .andExpect(jsonPath("$.linhas[1].ordem").value(2));
    }

    @Test
    void criarSemLinhasRetorna422() throws Exception {
        mockMvc.perform(post("/trajetos").contentType(MediaType.APPLICATION_JSON)
                        .content(payloadTrajeto("Ida", true, "[]")))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void criarComLinhasRepetidasRetorna400() throws Exception {
        mockMvc.perform(post("/trajetos").contentType(MediaType.APPLICATION_JSON)
                        .content(payloadTrajeto("Ida", true, ids(AZUL, AZUL))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void criarComMaisDeSeisLinhasRetorna422() throws Exception {
        mockMvc.perform(post("/trajetos").contentType(MediaType.APPLICATION_JSON)
                        .content(payloadTrajeto("Ida", true, "[1,2,3,4,5,6,7]")))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void criarComLinhaInexistenteRetorna404() throws Exception {
        mockMvc.perform(post("/trajetos").contentType(MediaType.APPLICATION_JSON)
                        .content(payloadTrajeto("Ida", true, "[99999]")))
                .andExpect(status().isNotFound());
    }

    @Test
    void obterInexistenteRetorna404() throws Exception {
        mockMvc.perform(get("/trajetos/99999")).andExpect(status().isNotFound());
    }

    @Test
    void listarEFiltrarAtivos() throws Exception {
        criarTrajeto("Ida", true, AZUL);
        criarTrajeto("Volta", false, AZUL);

        mockMvc.perform(get("/trajetos")).andExpect(jsonPath("$", hasSize(2)));
        mockMvc.perform(get("/trajetos").param("apenas_ativos", "true"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nome").value("Ida"));
    }

    @Test
    void atualizarTrocaAsLinhas() throws Exception {
        long id = criarTrajeto();

        mockMvc.perform(put("/trajetos/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(payloadTrajeto("Novo nome", true, ids(AZUL))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Novo nome"))
                .andExpect(jsonPath("$.linhas", hasSize(1)))
                .andExpect(jsonPath("$.linhas[0].nome_linha").value(AZUL));

        mockMvc.perform(get("/trajetos/" + id)).andExpect(jsonPath("$.linhas", hasSize(1)));
    }

    @Test
    void removerRetorna204EDepois404() throws Exception {
        long id = criarTrajeto();
        mockMvc.perform(delete("/trajetos/" + id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/trajetos/" + id)).andExpect(status().isNotFound());
    }

    // ---------- Verificacao / alertas ----------
    @Test
    void verificarUsaAPiorLinha() throws Exception {
        postarRelato(CORAL, "ATRASO", null, 22);
        long id = criarTrajeto("Ida", true, CORAL, VERMELHA);

        mockMvc.perform(post("/trajetos/" + id + "/verificar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alerta.status").value("CRITICO"))
                .andExpect(jsonPath("$.alerta.linha_mais_afetada").value(CORAL))
                .andExpect(jsonPath("$.linhas", hasSize(2)));
    }

    @Test
    void verificarTudoNormal() throws Exception {
        long id = criarTrajeto("Ida", true, AZUL);

        mockMvc.perform(post("/trajetos/" + id + "/verificar"))
                .andExpect(jsonPath("$.alerta.status").value("NORMAL"))
                .andExpect(jsonPath("$.alerta.linha_mais_afetada").doesNotExist());
    }

    @Test
    void verificarTrajetoInativoRetorna400() throws Exception {
        long id = criarTrajeto("Inativo", false, AZUL);
        mockMvc.perform(post("/trajetos/" + id + "/verificar")).andExpect(status().isBadRequest());
    }

    @Test
    void verificarTrajetoInexistenteRetorna404() throws Exception {
        mockMvc.perform(post("/trajetos/99999/verificar")).andExpect(status().isNotFound());
    }

    @Test
    void historicoVemDoMaisRecenteParaOMaisAntigo() throws Exception {
        long id = criarTrajeto("Ida", true, CORAL);
        mockMvc.perform(post("/trajetos/" + id + "/verificar")).andExpect(status().isOk());

        postarRelato(CORAL, "ATRASO", null, 7);
        mockMvc.perform(post("/trajetos/" + id + "/verificar")).andExpect(status().isOk());

        mockMvc.perform(get("/trajetos/" + id + "/alertas"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].status").value("MODERADO"))
                .andExpect(jsonPath("$[1].status").value("NORMAL"));
    }
}
