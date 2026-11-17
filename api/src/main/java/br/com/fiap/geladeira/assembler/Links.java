package br.com.fiap.geladeira.assembler;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

/** Ajuda a montar links com parametro de query (ex.: /api/doacoes?geladeiraId=3). */
final class Links {

    private Links() {
    }

    static Link comQuery(WebMvcLinkBuilder base, String parametro, Object valor, String rel) {
        String uri = base.toUriComponentsBuilder().queryParam(parametro, valor).build().toUriString();
        return Link.of(uri, rel);
    }
}
