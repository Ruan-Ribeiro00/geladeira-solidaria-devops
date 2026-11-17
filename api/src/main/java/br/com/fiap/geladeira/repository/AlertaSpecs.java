package br.com.fiap.geladeira.repository;

import br.com.fiap.geladeira.model.Alerta;
import br.com.fiap.geladeira.model.NivelAlerta;
import br.com.fiap.geladeira.model.TipoAlerta;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/** Filtro dinamico do painel de alertas. */
public final class AlertaSpecs {

    private AlertaSpecs() {
    }

    public static Specification<Alerta> comFiltro(Long geladeiraId, TipoAlerta tipo, NivelAlerta nivel, Boolean resolvido) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (geladeiraId != null) {
                p.add(cb.equal(root.get("geladeira").get("id"), geladeiraId));
            }
            if (tipo != null) {
                p.add(cb.equal(root.get("tipo"), tipo));
            }
            if (nivel != null) {
                p.add(cb.equal(root.get("nivel"), nivel));
            }
            if (resolvido != null) {
                p.add(cb.equal(root.get("resolvido"), resolvido));
            }
            return cb.and(p.toArray(Predicate[]::new));
        };
    }
}
