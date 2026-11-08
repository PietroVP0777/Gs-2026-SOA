package br.com.fiap.transitoclaro.domain;

import java.util.Collection;
import java.util.Comparator;

/** Regras de negocio puras (sem banco, sem HTTP): faceis de testar. */
public final class Regras {

    private Regras() {
    }

    /** O status do trajeto e o da pior linha. */
    public static StatusLinha piorStatus(Collection<StatusLinha> statuses) {
        return statuses.stream()
                .max(Comparator.comparingInt(StatusLinha::getGravidade))
                .orElse(StatusLinha.NORMAL);
    }

    /** Mensagem sobre a linha, em linguagem simples. */
    public static String mensagemDaLinha(StatusLinha status, int totalRelatos, double atrasoMedio) {
        if (totalRelatos == 0) {
            return "Sem relatos recentes. Nenhum problema informado pelos passageiros.";
        }
        return switch (status) {
            case CRITICO -> "Atencao: a linha esta com problemas (atraso medio de " + Math.round(atrasoMedio)
                    + " min ou muita lotacao). Considere uma rota alternativa.";
            case MODERADO -> "A linha tem alguns problemas relatados. Saia com um pouco mais de antecedencia.";
            case NORMAL -> "A linha esta funcionando normalmente segundo os relatos recentes.";
        };
    }

    /** Recomendacao para o trajeto do passageiro. */
    public static String recomendar(StatusLinha status, String linhaMaisAfetada) {
        return switch (status) {
            case CRITICO -> "Evite sair agora: " + linhaMaisAfetada
                    + " esta com problemas graves. Considere uma rota alternativa.";
            case MODERADO -> "Saia com antecedencia: " + linhaMaisAfetada + " tem problemas relatados.";
            case NORMAL -> "Tudo certo: pode sair no horario normal.";
        };
    }
}
