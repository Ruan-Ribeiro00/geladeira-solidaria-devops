package br.com.fiap.geladeira.dto;

import br.com.fiap.geladeira.model.TipoDoador;
import org.springframework.hateoas.server.core.Relation;

import java.time.LocalDate;

@Relation(itemRelation = "doador", collectionRelation = "doadores")
public record DoadorResponse(Long id, String nome, TipoDoador tipo, String telefone, String email, LocalDate dtCadastro) {
}
