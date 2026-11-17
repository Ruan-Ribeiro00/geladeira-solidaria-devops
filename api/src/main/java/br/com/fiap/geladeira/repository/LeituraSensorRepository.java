package br.com.fiap.geladeira.repository;

import br.com.fiap.geladeira.model.LeituraSensor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface LeituraSensorRepository extends JpaRepository<LeituraSensor, Long> {

    // query methods
    Page<LeituraSensor> findByGeladeiraId(Long idGeladeira, Pageable pageable);

    Optional<LeituraSensor> findFirstByGeladeiraIdOrderByDtLeituraDesc(Long idGeladeira);

    // JPQL: temperatura media da geladeira num periodo
    @Query("""
           select avg(l.temperaturaC)
             from LeituraSensor l
            where l.geladeira.id = :idGeladeira
              and l.dtLeitura >= :desde
           """)
    Double temperaturaMedia(@Param("idGeladeira") Long idGeladeira, @Param("desde") LocalDateTime desde);
}
