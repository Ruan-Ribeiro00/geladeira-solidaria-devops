package br.com.fiap.geladeira.controller;

import br.com.fiap.geladeira.assembler.CategoriaAssembler;
import br.com.fiap.geladeira.dto.CategoriaRequest;
import br.com.fiap.geladeira.dto.CategoriaResponse;
import br.com.fiap.geladeira.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/categorias")
@Tag(name = "Categorias", description = "Tipos de alimento e temperatura maxima segura (respostas em cache)")
public class CategoriaController {

    private final CategoriaService service;
    private final CategoriaAssembler assembler;

    public CategoriaController(CategoriaService service, CategoriaAssembler assembler) {
        this.service = service;
        this.assembler = assembler;
    }

    @GetMapping
    @Operation(summary = "Lista todas as categorias (lista pequena e fixa, por isso sem paginacao e em cache)")
    public CollectionModel<EntityModel<CategoriaResponse>> listar() {
        return assembler.toCollectionModel(service.listar())
                .add(linkTo(methodOn(CategoriaController.class).listar()).withSelfRel());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma categoria")
    public EntityModel<CategoriaResponse> buscar(@PathVariable Long id) {
        return assembler.toModel(service.buscar(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cria uma categoria (refrigeradas exigem tempMaxC)")
    public ResponseEntity<EntityModel<CategoriaResponse>> criar(@Valid @RequestBody CategoriaRequest req) {
        EntityModel<CategoriaResponse> model = assembler.toModel(service.criar(req));
        return ResponseEntity.created(model.getRequiredLink("self").toUri()).body(model);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualiza uma categoria")
    public EntityModel<CategoriaResponse> atualizar(@PathVariable Long id, @Valid @RequestBody CategoriaRequest req) {
        return assembler.toModel(service.atualizar(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Exclui uma categoria sem doacoes")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
