package br.com.fiap.transitoclaro;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.fiap.transitoclaro.domain.ClassificadorPorRelatos;
import br.com.fiap.transitoclaro.domain.ClassificadorStatus;
import br.com.fiap.transitoclaro.domain.Linha;
import br.com.fiap.transitoclaro.domain.NivelLotacao;
import br.com.fiap.transitoclaro.domain.RegraDeNegocioException;
import br.com.fiap.transitoclaro.domain.Regras;
import br.com.fiap.transitoclaro.domain.Relato;
import br.com.fiap.transitoclaro.domain.StatusLinha;
import br.com.fiap.transitoclaro.domain.TipoOcorrencia;
import br.com.fiap.transitoclaro.domain.TipoTransporte;
import br.com.fiap.transitoclaro.domain.Trajeto;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

/** Testes de dominio puros (sem Spring, sem banco). */
class RegrasTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 10, 12, 0);
    private static final Linha LINHA = new Linha("Linha Teste", TipoTransporte.METRO, "Operadora", null);

    private final ClassificadorStatus classificador = new ClassificadorPorRelatos();

    private static Relato atraso(int min) {
        return Relato.criar(LINHA, TipoOcorrencia.ATRASO, null, min, null, AGORA);
    }

    private static Relato lotacao(NivelLotacao n) {
        return Relato.criar(LINHA, TipoOcorrencia.LOTACAO, n, null, null, AGORA);
    }

    private static Relato falha() {
        return Relato.criar(LINHA, TipoOcorrencia.FALHA_OPERACIONAL, null, null, null, AGORA);
    }

    // ---------- Relato ----------
    @Test
    void relatoDeAtrasoValidoGuardaMinutosEDescricaoLimpa() {
        Relato r = Relato.criar(LINHA, TipoOcorrencia.ATRASO, null, 12, "  Trem parado  ", AGORA);
        assertEquals(12, r.getMinutosAtraso());
        assertEquals("Trem parado", r.getDescricao());
    }

    @Test
    void relatoDeAtrasoComMinutosInvalidosLancaExcecao() {
        for (Integer minutos : new Integer[] {null, 0, -5, 241}) {
            assertThrows(RegraDeNegocioException.class,
                    () -> Relato.criar(LINHA, TipoOcorrencia.ATRASO, null, minutos, null, AGORA));
        }
    }

    @Test
    void relatoDeLotacaoSemNivelLancaExcecao() {
        assertThrows(RegraDeNegocioException.class,
                () -> Relato.criar(LINHA, TipoOcorrencia.LOTACAO, null, null, null, AGORA));
    }

    @Test
    void relatoDeLotacaoNaoGuardaMinutosDeAtraso() {
        assertNull(Relato.criar(LINHA, TipoOcorrencia.LOTACAO, NivelLotacao.CHEIO, 30, null, AGORA)
                .getMinutosAtraso());
    }

    @Test
    void relatoComDescricaoMuitoLongaLancaExcecao() {
        String longa = "x".repeat(Relato.MAX_DESCRICAO + 1);
        assertThrows(RegraDeNegocioException.class,
                () -> Relato.criar(LINHA, TipoOcorrencia.FALHA_OPERACIONAL, null, null, longa, AGORA));
    }

    // ---------- Classificador ----------
    @Test
    void semRelatosEhNormal() {
        assertEquals(StatusLinha.NORMAL, classificador.classificar(List.of()));
    }

    @Test
    void atrasoMedioAltoEhCritico() {
        assertEquals(StatusLinha.CRITICO, classificador.classificar(List.of(atraso(20), atraso(16))));
    }

    @Test
    void atrasoMedioPequenoEhModerado() {
        assertEquals(StatusLinha.MODERADO, classificador.classificar(List.of(atraso(6), atraso(8))));
    }

    @Test
    void atrasoMuitoPequenoEhNormal() {
        assertEquals(StatusLinha.NORMAL, classificador.classificar(List.of(atraso(2))));
    }

    @Test
    void metadeLotadaEhCritico() {
        assertEquals(StatusLinha.CRITICO,
                classificador.classificar(List.of(lotacao(NivelLotacao.LOTADO), lotacao(NivelLotacao.TRANQUILO))));
    }

    @Test
    void maioriaCheiaEhModerado() {
        assertEquals(StatusLinha.MODERADO, classificador.classificar(
                List.of(lotacao(NivelLotacao.CHEIO), lotacao(NivelLotacao.CHEIO), lotacao(NivelLotacao.VAZIO))));
    }

    @Test
    void umaFalhaEhModeradoETresFalhasSaoCritico() {
        assertEquals(StatusLinha.MODERADO, classificador.classificar(List.of(falha())));
        assertEquals(StatusLinha.CRITICO, classificador.classificar(List.of(falha(), falha(), falha())));
    }

    // ---------- Regras ----------
    @Test
    void piorStatusEscolheOMaisGrave() {
        assertEquals(StatusLinha.CRITICO,
                Regras.piorStatus(List.of(StatusLinha.NORMAL, StatusLinha.CRITICO, StatusLinha.MODERADO)));
        assertEquals(StatusLinha.NORMAL, Regras.piorStatus(List.of()));
    }

    @Test
    void recomendarCitaALinhaAfetada() {
        assertTrue(Regras.recomendar(StatusLinha.CRITICO, "Linha 11-Coral").contains("Linha 11-Coral"));
        assertTrue(Regras.recomendar(StatusLinha.NORMAL, null).contains("horario normal"));
    }

    // ---------- Trajeto ----------
    private static Trajeto novoTrajeto() {
        return new Trajeto("Ida", "apelido", LocalTime.of(7, 30), true);
    }

    @Test
    void trajetoNaoAceitaLinhasRepetidas() {
        Linha a = new Linha("A", TipoTransporte.METRO, "Op", null);
        assertThrows(RegraDeNegocioException.class, () -> novoTrajeto().definirLinhas(List.of(a, a)));
    }

    @Test
    void trajetoNaoAceitaMaisDeSeisLinhas() {
        List<Linha> sete = IntStream.rangeClosed(1, 7)
                .mapToObj(i -> new Linha("L" + i, TipoTransporte.TREM, "Op", null)).toList();
        assertThrows(RegraDeNegocioException.class, () -> novoTrajeto().definirLinhas(sete));
    }

    @Test
    void trajetoNaoAceitaZeroLinhas() {
        assertThrows(RegraDeNegocioException.class, () -> novoTrajeto().definirLinhas(List.of()));
    }

    @Test
    void trajetoDefineAOrdemDasLinhas() {
        Trajeto t = novoTrajeto();
        t.definirLinhas(List.of(new Linha("X", TipoTransporte.METRO, "Op", null),
                new Linha("Y", TipoTransporte.BRT, "Op", null)));
        assertEquals(List.of(1, 2), t.getLinhas().stream().map(l -> l.getOrdem()).toList());
    }
}
