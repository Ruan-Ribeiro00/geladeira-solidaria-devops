package br.com.fiap.geladeira.assembler;

import br.com.fiap.geladeira.controller.AlertaController;
import br.com.fiap.geladeira.controller.DoacaoController;
import br.com.fiap.geladeira.controller.GeladeiraController;
import br.com.fiap.geladeira.dto.GeladeiraResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class GeladeiraAssembler implements RepresentationModelAssembler<GeladeiraResponse, EntityModel<GeladeiraResponse>> {

    @Override
    public EntityModel<GeladeiraResponse> toModel(GeladeiraResponse g) {
        return EntityModel.of(g,
                linkTo(methodOn(GeladeiraController.class).buscar(g.id())).withSelfRel(),
                linkTo(methodOn(GeladeiraController.class).situacao(g.id())).withRel("situacao"),
                Links.comQuery(linkTo(DoacaoController.class), "geladeiraId", g.id(), "doacoes"),
                linkTo(methodOn(GeladeiraController.class).leituras(g.id(), null)).withRel("leituras"),
                Links.comQuery(linkTo(AlertaController.class), "geladeiraId", g.id(), "alertas"),
                Link.of(linkTo(GeladeiraController.class).toUri().toString(), "geladeiras"));
    }
}
