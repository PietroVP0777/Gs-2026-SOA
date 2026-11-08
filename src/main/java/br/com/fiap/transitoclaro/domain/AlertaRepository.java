package br.com.fiap.transitoclaro.domain;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertaRepository extends JpaRepository<Alerta, Long> {

    List<Alerta> findByTrajetoIdOrderByCriadoEmDescIdDesc(Long trajetoId, Pageable pageable);
}
