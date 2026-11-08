package br.com.fiap.transitoclaro.domain;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RelatoRepository extends JpaRepository<Relato, Long> {

    @Override
    @EntityGraph(attributePaths = "linha")
    Page<Relato> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "linha")
    Page<Relato> findByLinhaId(Long linhaId, Pageable pageable);

    @EntityGraph(attributePaths = "linha")
    Page<Relato> findByTipo(TipoOcorrencia tipo, Pageable pageable);

    @EntityGraph(attributePaths = "linha")
    Page<Relato> findByLinhaIdAndTipo(Long linhaId, TipoOcorrencia tipo, Pageable pageable);

    List<Relato> findByLinhaIdAndCriadoEmGreaterThanEqual(Long linhaId, LocalDateTime limite);

    @EntityGraph(attributePaths = "linha")
    List<Relato> findByCriadoEmGreaterThanEqual(LocalDateTime limite);
}
