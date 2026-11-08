package br.com.fiap.transitoclaro.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrajetoRepository extends JpaRepository<Trajeto, Long> {

    List<Trajeto> findAllByOrderByHorarioSaidaAscIdAsc();

    List<Trajeto> findByAtivoTrueOrderByHorarioSaidaAscIdAsc();
}
