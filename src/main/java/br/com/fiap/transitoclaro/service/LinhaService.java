package br.com.fiap.transitoclaro.service;

import br.com.fiap.transitoclaro.domain.Linha;
import br.com.fiap.transitoclaro.domain.LinhaRepository;
import br.com.fiap.transitoclaro.domain.NaoEncontradoException;
import br.com.fiap.transitoclaro.domain.RegraDeNegocioException;
import br.com.fiap.transitoclaro.domain.TipoTransporte;
import br.com.fiap.transitoclaro.domain.TrajetoLinhaRepository;
import br.com.fiap.transitoclaro.web.Dtos.LinhaRequest;
import br.com.fiap.transitoclaro.web.Dtos.LinhaResponse;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LinhaService {

    private final LinhaRepository linhas;
    private final TrajetoLinhaRepository trajetoLinhas;

    public LinhaService(LinhaRepository linhas, TrajetoLinhaRepository trajetoLinhas) {
        this.linhas = linhas;
        this.trajetoLinhas = trajetoLinhas;
    }

    @Transactional(readOnly = true)
    public List<LinhaResponse> listar(TipoTransporte tipo) {
        List<Linha> resultado = tipo == null
                ? linhas.findAllByOrderByTipoAscNomeAsc()
                : linhas.findByTipoOrderByNomeAsc(tipo);
        return resultado.stream().map(LinhaResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public LinhaResponse obter(Long id) {
        return LinhaResponse.de(buscar(id));
    }

    @Transactional
    public LinhaResponse criar(LinhaRequest dados) {
        garantirNomeUnico(dados.nome(), null);
        Linha linha = new Linha(dados.nome(), dados.tipo(), dados.operadora(), dados.cor());
        return LinhaResponse.de(linhas.save(linha));
    }

    @Transactional
    public LinhaResponse atualizar(Long id, LinhaRequest dados) {
        Linha linha = buscar(id);
        garantirNomeUnico(dados.nome(), id);
        linha.atualizar(dados.nome(), dados.tipo(), dados.operadora(), dados.cor());
        return LinhaResponse.de(linha);
    }

    @Transactional
    public void remover(Long id) {
        Linha linha = buscar(id);
        if (trajetoLinhas.existsByLinhaId(id)) {
            throw new RegraDeNegocioException("A linha esta em uso em trajetos e nao pode ser removida.");
        }
        linhas.delete(linha);
    }

    private Linha buscar(Long id) {
        return linhas.findById(id).orElseThrow(() -> new NaoEncontradoException("Linha " + id + " nao encontrada."));
    }

    private void garantirNomeUnico(String nome, Long ignorarId) {
        linhas.findByNome(nome.trim())
                .filter(existente -> !existente.getId().equals(ignorarId))
                .ifPresent(existente -> {
                    throw new RegraDeNegocioException("Ja existe uma linha chamada '" + nome.trim() + "'.");
                });
    }
}
