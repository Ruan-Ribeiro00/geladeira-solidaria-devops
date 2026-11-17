package br.com.fiap.geladeira.controller;

import br.com.fiap.geladeira.assembler.DoadorAssembler;
import br.com.fiap.geladeira.dto.DoadorRequest;
import br.com.fiap.geladeira.dto.DoadorResponse;
import br.com.fiap.geladeira.model.TipoDoador;
import br.com.fiap.geladeira.service.DoadorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/doadores")
@PreAuthorize("hasAnyRole('ADMIN','VOLUNTARIO')")
@Tag(name = "Doadores", description = "Pessoas e comercios que doam (ADMIN e VOLUNTARIO)")
public class DoadorController {

    private final DoadorService service;
    private final DoadorAssembler assembler;
    private final PagedResourcesAssembler<DoadorResponse> pagedAssembler;

    public DoadorController(DoadorService service, DoadorAssembler assembler,
                            PagedResourcesAssembler<DoadorResponse> pagedAssembler) {
        this.service = service;
        this.assembler = assembler;
        this.pagedAssembler = pagedAssembler;
    }

    @GetMapping
    @Operation(summary = "Lista doadores (paginado), com filtro por parte do nome ou por tipo")
    public PagedModel<EntityModel<DoadorResponse>> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) TipoDoador tipo,
            @ParameterObject @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        return pagedAssembler.toModel(service.listar(nome, tipo, pageable), assembler);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um doador")
    public EntityModel<DoadorResponse> buscar(@PathVariable Long id) {
        return assembler.toModel(service.buscar(id));
    }

    @PostMapping
    @Operation(summary = "Cadastra um doador (ex.: comercio que doa pelo balcao, sem conta no app)")
    public ResponseEntity<EntityModel<DoadorResponse>> criar(@Valid @RequestBody DoadorRequest req) {
        EntityModel<DoadorResponse> model = assembler.toModel(service.criar(req));
        return ResponseEntity.created(model.getRequiredLink("self").toUri()).body(model);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um doador")
    public EntityModel<DoadorResponse> atualizar(@PathVariable Long id, @Valid @RequestBody DoadorRequest req) {
        return assembler.toModel(service.atualizar(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Exclui um doador sem doacoes (so ADMIN)")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
