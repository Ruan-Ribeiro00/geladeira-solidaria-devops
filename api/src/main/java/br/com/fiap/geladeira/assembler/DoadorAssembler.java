package br.com.fiap.geladeira.assembler;

import br.com.fiap.geladeira.controller.DoacaoController;
import br.com.fiap.geladeira.controller.DoadorController;
import br.com.fiap.geladeira.dto.DoadorResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class DoadorAssembler implements RepresentationModelAssembler<DoadorResponse, EntityModel<DoadorResponse>> {

    @Override
    public EntityModel<DoadorResponse> toModel(DoadorResponse d) {
        return EntityModel.of(d,
                linkTo(methodOn(DoadorController.class).buscar(d.id())).withSelfRel(),
                Links.comQuery(linkTo(DoacaoController.class), "doadorId", d.id(), "doacoes"));
    }
}
