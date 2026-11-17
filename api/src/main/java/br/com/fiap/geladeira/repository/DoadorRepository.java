package br.com.fiap.geladeira.repository;

import br.com.fiap.geladeira.model.Doador;
import br.com.fiap.geladeira.model.TipoDoador;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoadorRepository extends JpaRepository<Doador, Long> {

    Page<Doador> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    Page<Doador> findByTipo(TipoDoador tipo, Pageable pageable);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
