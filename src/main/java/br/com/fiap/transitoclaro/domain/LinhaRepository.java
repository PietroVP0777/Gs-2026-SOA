package br.com.fiap.transitoclaro.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LinhaRepository extends JpaRepository<Linha, Long> {

    List<Linha> findAllByOrderByTipoAscNomeAsc();

    List<Linha> findByTipoOrderByNomeAsc(TipoTransporte tipo);

    Optional<Linha> findByNome(String nome);
}
