package br.com.fiap.transitoclaro;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import br.com.fiap.transitoclaro.domain.AlertaRepository;
import br.com.fiap.transitoclaro.domain.LinhaRepository;
import br.com.fiap.transitoclaro.domain.RelatoRepository;
import br.com.fiap.transitoclaro.domain.TrajetoRepository;
import com.jayway.jsonpath.JsonPath;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/** Base dos testes de API: banco H2 em memoria, criado pelas migracoes Flyway (inclui as linhas iniciais). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class BaseApiTest {

    protected static final String AZUL = "Linha 1-Azul";
    protected static final String VERMELHA = "Linha 3-Vermelha";
    protected static final String CORAL = "Linha 11-Coral";

    private static final Set<String> LINHAS_INICIAIS = Set.of(
            "Linha 1-Azul", "Linha 2-Verde", "Linha 3-Vermelha", "Linha 11-Coral",
            "Linha 9-Esmeralda", "Corredor Expresso Tiradentes");

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected LinhaRepository linhaRepository;

    @Autowired
    protected RelatoRepository relatoRepository;

    @Autowired
    protected TrajetoRepository trajetoRepository;

    @Autowired
    protected AlertaRepository alertaRepository;

    @BeforeEach
    void limparBanco() {
        trajetoRepository.deleteAll();   // apaga trajetos (e seus alertas/ligacoes)
        relatoRepository.deleteAll();
        linhaRepository.findAll().stream()
                .filter(l -> !LINHAS_INICIAIS.contains(l.getNome()))
                .forEach(linhaRepository::delete);
    }

    protected long idLinha(String nome) {
        return linhaRepository.findByNome(nome).orElseThrow().getId();
    }

    protected static String json(String modelo, Object... args) {
        return modelo.formatted(args);
    }

    protected long postarRelato(String linha, String tipo, String nivel, Integer minutos) throws Exception {
        String corpo = """
                {"linha_id":%d,"tipo":"%s","nivel_lotacao":%s,"minutos_atraso":%s}
                """.formatted(idLinha(linha), tipo,
                nivel == null ? "null" : "\"" + nivel + "\"",
                minutos == null ? "null" : minutos.toString());
        String resposta = mockMvc.perform(post("/relatos").contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(resposta, "$.id")).longValue();
    }

    protected static String payloadTrajeto(String nome, boolean ativo, String linhaIds) {
        return """
                {"nome":"%s","apelido":"passageira-zl","horario_saida":"07:30:00","ativo":%s,"linha_ids":%s}
                """.formatted(nome, ativo, linhaIds);
    }

    protected long criarTrajeto(String nome, boolean ativo, String... linhas) throws Exception {
        StringBuilder ids = new StringBuilder("[");
        for (int i = 0; i < linhas.length; i++) {
            ids.append(i > 0 ? "," : "").append(idLinha(linhas[i]));
        }
        ids.append("]");
        String resposta = mockMvc.perform(post("/trajetos").contentType(MediaType.APPLICATION_JSON)
                        .content(payloadTrajeto(nome, ativo, ids.toString())))
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(resposta, "$.id")).longValue();
    }

    protected long criarTrajeto() throws Exception {
        return criarTrajeto("Ida", true, CORAL, VERMELHA);
    }
}
