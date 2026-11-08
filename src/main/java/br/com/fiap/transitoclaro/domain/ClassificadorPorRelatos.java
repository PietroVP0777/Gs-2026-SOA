package br.com.fiap.transitoclaro.domain;

import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Regras:
 * CRITICO  - atraso medio >= 15 min, ou >= 50% dos relatos de lotacao "LOTADO", ou >= 3 falhas;
 * MODERADO - atraso medio >= 5 min, ou >= 50% "CHEIO" ou pior, ou >= 1 falha;
 * NORMAL   - demais casos (inclui linha sem relatos recentes).
 */
@Component
public class ClassificadorPorRelatos implements ClassificadorStatus {

    public static final double ATRASO_CRITICO_MIN = 15;
    public static final double ATRASO_MODERADO_MIN = 5;
    public static final double PROPORCAO_LIMITE = 0.5;
    public static final int FALHAS_CRITICO = 3;

    @Override
    public StatusLinha classificar(List<Relato> relatos) {
        if (relatos.isEmpty()) {
            return StatusLinha.NORMAL;
        }

        double atrasoMedio = relatos.stream()
                .filter(r -> r.getTipo() == TipoOcorrencia.ATRASO && r.getMinutosAtraso() != null)
                .mapToInt(Relato::getMinutosAtraso)
                .average().orElse(0);

        List<Relato> comLotacao = relatos.stream().filter(r -> r.getNivelLotacao() != null).toList();
        double propLotado = proporcao(comLotacao, NivelLotacao.LOTADO);
        double propCheioOuMais = proporcao(comLotacao, NivelLotacao.CHEIO);

        long falhas = relatos.stream().filter(r -> r.getTipo() == TipoOcorrencia.FALHA_OPERACIONAL).count();

        if (atrasoMedio >= ATRASO_CRITICO_MIN || propLotado >= PROPORCAO_LIMITE || falhas >= FALHAS_CRITICO) {
            return StatusLinha.CRITICO;
        }
        if (atrasoMedio >= ATRASO_MODERADO_MIN || propCheioOuMais >= PROPORCAO_LIMITE || falhas >= 1) {
            return StatusLinha.MODERADO;
        }
        return StatusLinha.NORMAL;
    }

    /** Proporcao de relatos com lotacao igual ou pior que o minimo informado. */
    private static double proporcao(List<Relato> comLotacao, NivelLotacao minimo) {
        if (comLotacao.isEmpty()) {
            return 0;
        }
        long qtd = comLotacao.stream().filter(r -> r.getNivelLotacao().compareTo(minimo) >= 0).count();
        return qtd / (double) comLotacao.size();
    }
}
