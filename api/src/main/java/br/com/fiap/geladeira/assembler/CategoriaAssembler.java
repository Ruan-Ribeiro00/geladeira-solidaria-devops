package br.com.fiap.geladeira.assembler;

import br.com.fiap.geladeira.controller.CategoriaController;
import br.com.fiap.geladeira.controller.DoacaoController;
import br.com.fiap.geladeira.dto.CategoriaResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class CategoriaAssembler implements RepresentationModelAssembler<CategoriaResponse, EntityModel<CategoriaResponse>> {

    @Override
    public EntityModel<CategoriaResponse> toModel(CategoriaResponse c) {
        return EntityModel.of(c,
                linkTo(methodOn(CategoriaController.class).buscar(c.id())).withSelfRel(),
                Links.comQuery(linkTo(DoacaoController.class), "categoriaId", c.id(), "doacoes"),
                linkTo(methodOn(CategoriaController.class).listar()).withRel("categorias"));
    }
}
