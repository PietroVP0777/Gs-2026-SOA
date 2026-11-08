package br.com.fiap.transitoclaro.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TrajetoLinhaRepository extends JpaRepository<TrajetoLinha, Long> {

    boolean existsByLinhaId(Long linhaId);
}
