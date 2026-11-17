package br.com.fiap.geladeira.dto;

import org.springframework.hateoas.server.core.Relation;

import java.math.BigDecimal;

@Relation(itemRelation = "categoria", collectionRelation = "categorias")
public record CategoriaResponse(Long id, String nome, boolean refrigerado, BigDecimal tempMaxC) {
}
