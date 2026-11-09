package br.com.fiap3espg.heatsafe.domain.estacao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstacaoRepository extends JpaRepository<Estacao, Long> {
    Page<Estacao> findAllByAtivaTrue(Pageable paginacao);

    Optional<Estacao> findByCodigoAndAtivaTrue(String codigo);

    boolean existsByCodigo(String codigo);
}
