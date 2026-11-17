package br.com.fiap.geladeira.assembler;

import br.com.fiap.geladeira.controller.CategoriaController;
import br.com.fiap.geladeira.controller.DoacaoController;
import br.com.fiap.geladeira.controller.DoadorController;
import br.com.fiap.geladeira.controller.GeladeiraController;
import br.com.fiap.geladeira.dto.DoacaoResponse;
import br.com.fiap.geladeira.model.StatusDoacao;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Alem dos links de navegacao, a doacao DISPONIVEL ganha os links das acoes possiveis
 * (retirar e descartar). Quando ja foi retirada/descartada, esses links somem:
 * o cliente descobre pelo proprio JSON o que pode fazer.
 */
@Component
public class DoacaoAssembler implements RepresentationModelAssembler<DoacaoResponse, EntityModel<DoacaoResponse>> {

    @Override
    public EntityModel<DoacaoResponse> toModel(DoacaoResponse d) {
        EntityModel<DoacaoResponse> model = EntityModel.of(d,
                linkTo(methodOn(DoacaoController.class).buscar(d.id(), null)).withSelfRel(),
                linkTo(methodOn(GeladeiraController.class).buscar(d.geladeiraId())).withRel("geladeira"),
                linkTo(methodOn(DoadorController.class).buscar(d.doadorId())).withRel("doador"),
                linkTo(methodOn(CategoriaController.class).buscar(d.categoriaId())).withRel("categoria"));
        if (d.status() == StatusDoacao.DISPONIVEL) {
            model.add(linkTo(methodOn(DoacaoController.class).registrarRetirada(d.id())).withRel("retirar"));
            model.add(linkTo(methodOn(DoacaoController.class).registrarDescarte(d.id())).withRel("descartar"));
        }
        return model;
    }
}
