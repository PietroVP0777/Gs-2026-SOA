package br.com.fiap.transitoclaro.domain;

import java.util.List;

/** Estrategia que transforma relatos recentes em um status simples para o passageiro. */
public interface ClassificadorStatus {

    StatusLinha classificar(List<Relato> relatosRecentes);
}
