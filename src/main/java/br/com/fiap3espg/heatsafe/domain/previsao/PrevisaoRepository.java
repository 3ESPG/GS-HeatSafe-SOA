package br.com.fiap3espg.heatsafe.domain.previsao;

import br.com.fiap3espg.heatsafe.domain.estacao.Estacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrevisaoRepository extends JpaRepository<Previsao, Long> {
    Page<Previsao> findByEstacao(Estacao estacao, Pageable paginacao);
}
