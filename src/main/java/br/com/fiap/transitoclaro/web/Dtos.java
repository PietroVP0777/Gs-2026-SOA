package br.com.fiap.transitoclaro.web;

import br.com.fiap.transitoclaro.domain.Alerta;
import br.com.fiap.transitoclaro.domain.Linha;
import br.com.fiap.transitoclaro.domain.NivelLotacao;
import br.com.fiap.transitoclaro.domain.Relato;
import br.com.fiap.transitoclaro.domain.StatusLinha;
import br.com.fiap.transitoclaro.domain.TipoOcorrencia;
import br.com.fiap.transitoclaro.domain.TipoTransporte;
import br.com.fiap.transitoclaro.domain.Trajeto;
import br.com.fiap.transitoclaro.domain.TrajetoLinha;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/** Contratos de entrada e saida da API (JSON em snake_case). */
public final class Dtos {

    private Dtos() {
    }

    // ================= entrada =================
    public record LinhaRequest(
            @NotBlank @Size(min = 2, max = 80) String nome,
            @NotNull TipoTransporte tipo,
            @NotBlank @Size(min = 2, max = 80) String operadora,
            @Size(max = 30) String cor) {
    }

    public record RelatoRequest(
            @NotNull @Min(1) Long linhaId,
            @NotNull TipoOcorrencia tipo,
            NivelLotacao nivelLotacao,
            @Min(1) @Max(240) Integer minutosAtraso,
            @Size(max = 280) String descricao) {
    }

    public record TrajetoRequest(
            @NotBlank @Size(min = 2, max = 80) String nome,
            @NotBlank @Size(min = 2, max = 40) String apelido,
            @NotNull LocalTime horarioSaida,
            Boolean ativo,
            @NotNull @Size(min = 1, max = 6) List<@NotNull @Min(1) Long> linhaIds) {
    }

    // ================= saida =================
    public record LinhaResponse(Long id, String nome, TipoTransporte tipo, String operadora, String cor) {
        public static LinhaResponse de(Linha l) {
            return new LinhaResponse(l.getId(), l.getNome(), l.getTipo(), l.getOperadora(), l.getCor());
        }
    }

    public record RelatoResponse(Long id, Long linhaId, String linhaNome, TipoOcorrencia tipo,
                                 NivelLotacao nivelLotacao, Integer minutosAtraso, String descricao,
                                 LocalDateTime criadoEm) {
        public static RelatoResponse de(Relato r) {
            return new RelatoResponse(r.getId(), r.getLinha().getId(), r.getLinha().getNome(), r.getTipo(),
                    r.getNivelLotacao(), r.getMinutosAtraso(), r.getDescricao(), r.getCriadoEm());
        }
    }

    public record PaginaResponse<T>(List<T> itens, int pagina, int tamanho, long total) {
    }

    public record StatusLinhaResponse(Long linhaId, String linha, TipoTransporte tipo, StatusLinha status,
                                      double atrasoMedioMinutos, NivelLotacao lotacaoPredominante,
                                      int totalRelatos, int janelaMinutos, String mensagem) {
    }

    public record LinhaAtrasoResponse(Long linhaId, String linha, long totalAtrasos, double atrasoMedioMinutos) {
    }

    public record EstatisticasResponse(int periodoHoras, int totalRelatos, Map<String, Long> porTipo,
                                       List<LinhaAtrasoResponse> linhasComMaisAtrasos) {
    }

    public record TrajetoLinhaResponse(Long linhaId, String nomeLinha, int ordem) {
        static TrajetoLinhaResponse de(TrajetoLinha tl) {
            return new TrajetoLinhaResponse(tl.getLinha().getId(), tl.getLinha().getNome(), tl.getOrdem());
        }
    }

    public record TrajetoResponse(Long id, String nome, String apelido, LocalTime horarioSaida, boolean ativo,
                                  LocalDateTime criadoEm, List<TrajetoLinhaResponse> linhas) {
        public static TrajetoResponse de(Trajeto t) {
            return new TrajetoResponse(t.getId(), t.getNome(), t.getApelido(), t.getHorarioSaida(), t.isAtivo(),
                    t.getCriadoEm(), t.getLinhas().stream().map(TrajetoLinhaResponse::de).toList());
        }
    }

    public record AlertaResponse(Long id, Long trajetoId, StatusLinha status, String mensagem,
                                 String linhaMaisAfetada, LocalDateTime criadoEm) {
        public static AlertaResponse de(Alerta a) {
            return new AlertaResponse(a.getId(), a.getTrajeto().getId(), a.getStatus(), a.getMensagem(),
                    a.getLinhaMaisAfetada(), a.getCriadoEm());
        }
    }

    public record VerificacaoResponse(AlertaResponse alerta, List<StatusLinhaResponse> linhas) {
    }
}
