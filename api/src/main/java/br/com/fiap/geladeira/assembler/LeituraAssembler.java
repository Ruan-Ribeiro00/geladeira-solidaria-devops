package br.com.fiap.geladeira.assembler;

import br.com.fiap.geladeira.controller.GeladeiraController;
import br.com.fiap.geladeira.dto.LeituraResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class LeituraAssembler implements RepresentationModelAssembler<LeituraResponse, EntityModel<LeituraResponse>> {

    @Override
    public EntityModel<LeituraResponse> toModel(LeituraResponse l) {
        return EntityModel.of(l,
                linkTo(methodOn(GeladeiraController.class).buscar(l.geladeiraId())).withRel("geladeira"),
                linkTo(methodOn(GeladeiraController.class).situacao(l.geladeiraId())).withRel("situacao"));
    }
}
