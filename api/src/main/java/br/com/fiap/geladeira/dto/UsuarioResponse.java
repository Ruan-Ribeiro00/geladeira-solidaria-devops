package br.com.fiap.geladeira.dto;

import br.com.fiap.geladeira.model.Papel;
import org.springframework.hateoas.server.core.Relation;

@Relation(itemRelation = "usuario", collectionRelation = "usuarios")
public record UsuarioResponse(Long id, String nome, String email, Papel papel, Long doadorId) {
}
