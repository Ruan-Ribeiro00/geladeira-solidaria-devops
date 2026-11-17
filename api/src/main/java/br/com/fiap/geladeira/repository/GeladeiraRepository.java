package br.com.fiap.geladeira.repository;

import br.com.fiap.geladeira.model.Geladeira;
import br.com.fiap.geladeira.model.StatusDoacao;
import br.com.fiap.geladeira.model.StatusGeladeira;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface GeladeiraRepository extends JpaRepository<Geladeira, Long> {

    // ---------------------------------------------------------------- query methods
    Page<Geladeira> findByBairroContainingIgnoreCase(String bairro, Pageable pageable);

    Page<Geladeira> findByStatus(StatusGeladeira status, Pageable pageable);

    Page<Geladeira> findByBairroContainingIgnoreCaseAndStatus(String bairro, StatusGeladeira status, Pageable pageable);

    List<Geladeira> findByStatusOrderByIdAsc(StatusGeladeira status);

    // ---------------------------------------------------------------- JPQL

    /** Quantos kg de alimento estao disponiveis dentro da geladeira agora. */
    @Query("""
           select coalesce(sum(d.quantidadeKg), 0)
             from Doacao d
            where d.geladeira.id = :idGeladeira
              and d.status = :disponivel
           """)
    BigDecimal somarOcupacaoKg(@Param("idGeladeira") Long idGeladeira,
                               @Param("disponivel") StatusDoacao disponivel);

    /**
     * Ocupacao de todas as geladeiras ativas, da mais cheia para a mais vazia.
     * Usado no ranking de ocupacao e para decidir onde mandar novas doacoes.
     */
    @Query("""
           select g.id as id, g.nome as nome, g.bairro as bairro,
                  g.capacidadeKg as capacidadeKg,
                  coalesce(sum(d.quantidadeKg), 0) as ocupadoKg
             from Geladeira g
             left join g.doacoes d on d.status = :disponivel
            where g.status = :ativa
            group by g.id, g.nome, g.bairro, g.capacidadeKg
            order by coalesce(sum(d.quantidadeKg), 0) / g.capacidadeKg desc
           """)
    List<OcupacaoGeladeira> rankingOcupacao(@Param("ativa") StatusGeladeira ativa,
                                            @Param("disponivel") StatusDoacao disponivel);

    /**
     * Geladeiras ativas com pelo menos {@code kg} livres, da que tem mais espaco para a que tem menos.
     * E daqui que sai a sugestao quando uma doacao nao cabe na geladeira escolhida.
     */
    @Query("""
           select g.id as id, g.nome as nome, g.bairro as bairro,
                  g.capacidadeKg as capacidadeKg,
                  coalesce(sum(d.quantidadeKg), 0) as ocupadoKg
             from Geladeira g
             left join g.doacoes d on d.status = :disponivel
            where g.status = :ativa
            group by g.id, g.nome, g.bairro, g.capacidadeKg
           having g.capacidadeKg - coalesce(sum(d.quantidadeKg), 0) >= :kg
            order by g.capacidadeKg - coalesce(sum(d.quantidadeKg), 0) desc
           """)
    List<OcupacaoGeladeira> comEspacoLivre(@Param("kg") BigDecimal kg,
                                           @Param("ativa") StatusGeladeira ativa,
                                           @Param("disponivel") StatusDoacao disponivel);
}
