package br.com.fiap.transitoclaro.service;

import br.com.fiap.transitoclaro.domain.Linha;
import br.com.fiap.transitoclaro.domain.LinhaRepository;
import br.com.fiap.transitoclaro.domain.NaoEncontradoException;
import br.com.fiap.transitoclaro.domain.Trajeto;
import br.com.fiap.transitoclaro.domain.TrajetoRepository;
import br.com.fiap.transitoclaro.web.Dtos.TrajetoRequest;
import br.com.fiap.transitoclaro.web.Dtos.TrajetoResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Casos de uso de trajetos (CRUD). */
@Service
public class TrajetoService {

    private final TrajetoRepository trajetos;
    private final LinhaRepository linhas;

    public TrajetoService(TrajetoRepository trajetos, LinhaRepository linhas) {
        this.trajetos = trajetos;
        this.linhas = linhas;
    }

    @Transactional
    public TrajetoResponse criar(TrajetoRequest dados) {
        var trajeto = new Trajeto(dados.nome().trim(), dados.apelido().trim(), dados.horarioSaida(), estaAtivo(dados));
        trajeto.definirLinhas(resolverLinhas(dados.linhaIds()));
        return TrajetoResponse.de(trajetos.save(trajeto));
    }

    @Transactional(readOnly = true)
    public List<TrajetoResponse> listar(boolean apenasAtivos) {
        List<Trajeto> resultado = apenasAtivos
                ? trajetos.findByAtivoTrueOrderByHorarioSaidaAscIdAsc()
                : trajetos.findAllByOrderByHorarioSaidaAscIdAsc();
        return resultado.stream().map(TrajetoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public TrajetoResponse obter(Long id) {
        return TrajetoResponse.de(buscar(id));
    }

    @Transactional
    public TrajetoResponse atualizar(Long id, TrajetoRequest dados) {
        Trajeto trajeto = buscar(id);
        trajeto.atualizarDados(dados.nome().trim(), dados.apelido().trim(), dados.horarioSaida(), estaAtivo(dados));
        trajeto.definirLinhas(resolverLinhas(dados.linhaIds()));
        return TrajetoResponse.de(trajeto);
    }

    @Transactional
    public void remover(Long id) {
        trajetos.delete(buscar(id));
    }

    private Trajeto buscar(Long id) {
        return trajetos.findById(id).orElseThrow(() -> new NaoEncontradoException("Trajeto " + id + " nao encontrado."));
    }

    private static boolean estaAtivo(TrajetoRequest dados) {
        return dados.ativo() == null || dados.ativo();
    }

    /** Converte os ids recebidos em linhas, mantendo a ordem e acusando ids inexistentes. */
    private List<Linha> resolverLinhas(List<Long> ids) {
        Map<Long, Linha> encontradas = linhas.findAllById(ids).stream()
                .collect(Collectors.toMap(Linha::getId, Function.identity()));

        List<Linha> ordenadas = new ArrayList<>();
        for (Long id : ids) {
            Linha linha = encontradas.get(id);
            if (linha == null) {
                throw new NaoEncontradoException("Linha " + id + " nao encontrada.");
            }
            ordenadas.add(linha);
        }
        return ordenadas;
    }
}
