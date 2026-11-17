package br.com.fiap.geladeira.controller;

import br.com.fiap.geladeira.assembler.AlertaAssembler;
import br.com.fiap.geladeira.dto.AlertaResponse;
import br.com.fiap.geladeira.model.NivelAlerta;
import br.com.fiap.geladeira.model.TipoAlerta;
import br.com.fiap.geladeira.service.AlertaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alertas")
@PreAuthorize("hasAnyRole('ADMIN','VOLUNTARIO')")
@Tag(name = "Alertas", description = "Painel de avisos para voluntarios e responsaveis")
public class AlertaController {

    private final AlertaService service;
    private final AlertaAssembler assembler;
    private final PagedResourcesAssembler<AlertaResponse> pagedAssembler;

    public AlertaController(AlertaService service, AlertaAssembler assembler,
                            PagedResourcesAssembler<AlertaResponse> pagedAssembler) {
        this.service = service;
        this.assembler = assembler;
        this.pagedAssembler = pagedAssembler;
    }

    @GetMapping
    @Operation(summary = "Lista alertas (paginado), com filtros opcionais. Ex.: ?resolvido=false&tipo=TEMPERATURA")
    public PagedModel<EntityModel<AlertaResponse>> listar(
            @RequestParam(required = false) Long geladeiraId,
            @RequestParam(required = false) TipoAlerta tipo,
            @RequestParam(required = false) NivelAlerta nivel,
            @RequestParam(required = false) Boolean resolvido,
            @ParameterObject @PageableDefault(size = 10, sort = "dtAlerta", direction = Sort.Direction.DESC) Pageable pageable) {
        return pagedAssembler.toModel(service.listar(geladeiraId, tipo, nivel, resolvido, pageable), assembler);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um alerta")
    public EntityModel<AlertaResponse> buscar(@PathVariable Long id) {
        return assembler.toModel(service.buscar(id));
    }

    @PatchMapping("/{id}/resolver")
    @Operation(summary = "Marca o alerta como resolvido")
    public EntityModel<AlertaResponse> resolver(@PathVariable Long id) {
        return assembler.toModel(service.resolver(id));
    }
}
