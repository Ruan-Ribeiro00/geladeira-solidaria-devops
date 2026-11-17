package br.com.fiap.geladeira.repository;

import br.com.fiap.geladeira.model.Alerta;
import br.com.fiap.geladeira.model.TipoAlerta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlertaRepository extends JpaRepository<Alerta, Long>, JpaSpecificationExecutor<Alerta> {

    @Override
    @EntityGraph(attributePaths = {"geladeira", "doacao"})
    Page<Alerta> findAll(Specification<Alerta> spec, Pageable pageable);

    /** Evita abrir um segundo alerta igual enquanto o primeiro nao for resolvido. */
    boolean existsByGeladeiraIdAndTipoAndResolvidoFalse(Long idGeladeira, TipoAlerta tipo);

    boolean existsByDoacaoIdAndTipoAndResolvidoFalse(Long idDoacao, TipoAlerta tipo);

    long countByResolvidoFalse();

    /** Antes de apagar uma doacao, os alertas dela continuam no historico, mas sem o vinculo. */
    @Modifying
    @Query("update Alerta a set a.doacao = null where a.doacao.id = :idDoacao")
    int desvincularDoacao(@Param("idDoacao") Long idDoacao);
}
