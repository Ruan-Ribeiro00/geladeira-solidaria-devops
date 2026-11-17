package br.com.fiap.geladeira.repository;

import br.com.fiap.geladeira.dto.DoacaoFiltro;
import br.com.fiap.geladeira.model.Doacao;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Filtro dinamico de doacoes com Specification (Criteria API).
 * Cada campo do filtro so entra no WHERE se vier preenchido, entao a mesma
 * consulta atende "doacoes da geladeira 3", "laticinios em risco CRITICO",
 * "tudo que vence ate sexta" e qualquer combinacao entre eles.
 */
public final class DoacaoSpecs {

    private DoacaoSpecs() {
    }

    public static Specification<Doacao> comFiltro(DoacaoFiltro f) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();

            if (f.geladeiraId() != null) {
                p.add(cb.equal(root.get("geladeira").get("id"), f.geladeiraId()));
            }
            if (f.doadorId() != null) {
                p.add(cb.equal(root.get("doador").get("id"), f.doadorId()));
            }
            if (f.categoriaId() != null) {
                p.add(cb.equal(root.get("categoria").get("id"), f.categoriaId()));
            }
            if (f.status() != null) {
                p.add(cb.equal(root.get("status"), f.status()));
            }
            if (f.nivelRisco() != null) {
                p.add(cb.equal(root.get("nivelRisco"), f.nivelRisco()));
            }
            if (f.bairro() != null && !f.bairro().isBlank()) {
                p.add(cb.like(cb.lower(root.get("geladeira").get("bairro")), "%" + f.bairro().toLowerCase() + "%"));
            }
            if (f.descricao() != null && !f.descricao().isBlank()) {
                p.add(cb.like(cb.lower(root.get("descricao")), "%" + f.descricao().toLowerCase() + "%"));
            }
            if (f.validadeAte() != null) {
                p.add(cb.lessThanOrEqualTo(root.get("dtValidade"), f.validadeAte()));
            }
            if (f.validadeDe() != null) {
                p.add(cb.greaterThanOrEqualTo(root.get("dtValidade"), f.validadeDe()));
            }
            if (Boolean.TRUE.equals(f.refrigerado())) {
                p.add(cb.isTrue(root.get("categoria").get("refrigerado")));
            }

            return cb.and(p.toArray(Predicate[]::new));
        };
    }
}
