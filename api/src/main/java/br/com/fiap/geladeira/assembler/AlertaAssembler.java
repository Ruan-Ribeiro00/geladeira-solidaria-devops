package br.com.fiap.geladeira.assembler;

import br.com.fiap.geladeira.controller.AlertaController;
import br.com.fiap.geladeira.controller.DoacaoController;
import br.com.fiap.geladeira.controller.GeladeiraController;
import br.com.fiap.geladeira.dto.AlertaResponse;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class AlertaAssembler implements RepresentationModelAssembler<AlertaResponse, EntityModel<AlertaResponse>> {

    @Override
    public EntityModel<AlertaResponse> toModel(AlertaResponse a) {
        EntityModel<AlertaResponse> model = EntityModel.of(a,
                linkTo(methodOn(AlertaController.class).buscar(a.id())).withSelfRel(),
                linkTo(methodOn(GeladeiraController.class).buscar(a.geladeiraId())).withRel("geladeira"));
        if (a.doacaoId() != null) {
            model.add(linkTo(methodOn(DoacaoController.class).buscar(a.doacaoId(), null)).withRel("doacao"));
        }
        if (!a.resolvido()) {
            model.add(linkTo(methodOn(AlertaController.class).resolver(a.id())).withRel("resolver"));
        }
        return model;
    }
}
