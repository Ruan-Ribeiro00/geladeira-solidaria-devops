package br.com.fiap.geladeira.repository;

import br.com.fiap.geladeira.model.Doacao;
import br.com.fiap.geladeira.model.StatusDoacao;
import br.com.fiap.geladeira.model.StatusGeladeira;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DoacaoRepository extends JpaRepository<Doacao, Long>, JpaSpecificationExecutor<Doacao> {

    /** Filtro dinamico (Specification) ja trazendo geladeira, doador e categoria num JOIN so, sem N+1. */
    @Override
    @EntityGraph(attributePaths = {"geladeira", "doador", "categoria"})
    Page<Doacao> findAll(Specification<Doacao> spec, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"geladeira", "doador", "categoria"})
    Optional<Doacao> findById(Long id);

    // ---------------------------------------------------------------- query methods
    @EntityGraph(attributePaths = {"geladeira", "doador", "categoria"})
    Page<Doacao> findByGeladeiraIdAndStatus(Long idGeladeira, StatusDoacao status, Pageable pageable);

    @EntityGraph(attributePaths = {"geladeira", "categoria"})
    List<Doacao> findByStatus(StatusDoacao status);

    boolean existsByGeladeiraId(Long idGeladeira);

    boolean existsByDoadorId(Long idDoador);

    boolean existsByCategoriaId(Long idCategoria);

    // ---------------------------------------------------------------- JPQL

    /**
     * Lista de distribuicao prioritaria: alimentos disponiveis em geladeiras ativas
     * que vencem ate {@code limite}, do que vence primeiro para o que vence depois.
     */
    @Query("""
           select d
             from Doacao d
             join fetch d.geladeira g
             join fetch d.categoria c
             join fetch d.doador
            where d.status = :disponivel
              and g.status = :ativa
              and d.dtValidade between :hoje and :limite
            order by d.dtValidade asc, d.quantidadeKg desc
           """)
    List<Doacao> listaDistribuicaoPrioritaria(@Param("hoje") LocalDate hoje,
                                              @Param("limite") LocalDate limite,
                                              @Param("disponivel") StatusDoacao disponivel,
                                              @Param("ativa") StatusGeladeira ativa);
}
