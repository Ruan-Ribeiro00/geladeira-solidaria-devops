package br.com.fiap.geladeira.controller;

import br.com.fiap.geladeira.assembler.GeladeiraAssembler;
import br.com.fiap.geladeira.assembler.LeituraAssembler;
import br.com.fiap.geladeira.dto.*;
import br.com.fiap.geladeira.model.StatusGeladeira;
import br.com.fiap.geladeira.service.GeladeiraService;
import br.com.fiap.geladeira.service.LeituraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/geladeiras")
@Validated
@Tag(name = "Geladeiras", description = "Pontos de doacao da rede (consulta publica, escrita so ADMIN)")
public class GeladeiraController {

    private final GeladeiraService service;
    private final LeituraService leituraService;
    private final GeladeiraAssembler assembler;
    private final LeituraAssembler leituraAssembler;
    private final PagedResourcesAssembler<GeladeiraResponse> pagedAssembler;
    private final PagedResourcesAssembler<LeituraResponse> pagedLeituras;

    public GeladeiraController(GeladeiraService service, LeituraService leituraService, GeladeiraAssembler assembler,
                               LeituraAssembler leituraAssembler,
                               PagedResourcesAssembler<GeladeiraResponse> pagedAssembler,
                               PagedResourcesAssembler<LeituraResponse> pagedLeituras) {
        this.service = service;
        this.leituraService = leituraService;
        this.assembler = assembler;
        this.leituraAssembler = leituraAssembler;
        this.pagedAssembler = pagedAssembler;
        this.pagedLeituras = pagedLeituras;
    }

    @GetMapping
    @Operation(summary = "Lista as geladeiras (paginado), com filtro opcional por bairro e status",
            description = "Ex.: ?bairro=capao&status=ATIVA&page=0&size=5&sort=capacidadeKg,desc")
    public PagedModel<EntityModel<GeladeiraResponse>> listar(
            @RequestParam(required = false) String bairro,
            @RequestParam(required = false) StatusGeladeira status,
            @ParameterObject @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        return pagedAssembler.toModel(service.listar(bairro, status, pageable), assembler);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhe da geladeira, com ocupacao atual (resposta em cache)")
    public EntityModel<GeladeiraResponse> buscar(@PathVariable Long id) {
        return assembler.toModel(service.buscar(id));
    }

    @GetMapping("/{id}/situacao")
    @Operation(summary = "Situacao da geladeira (RN1): fora de operacao, sem sinal, temperatura, lotada, estoque baixo ou normal")
    public EntityModel<SituacaoGeladeiraResponse> situacao(@PathVariable Long id) {
        return EntityModel.of(service.situacao(id),
                linkTo(methodOn(GeladeiraController.class).situacao(id)).withSelfRel(),
                linkTo(methodOn(GeladeiraController.class).buscar(id)).withRel("geladeira"));
    }

    @GetMapping("/{id}/leituras")
    @PreAuthorize("hasAnyRole('ADMIN','VOLUNTARIO')")
    @Operation(summary = "Historico de leituras do sensor da geladeira (mais recentes primeiro)")
    public PagedModel<EntityModel<LeituraResponse>> leituras(
            @PathVariable Long id,
            @ParameterObject @PageableDefault(size = 20, sort = "dtLeitura", direction = Sort.Direction.DESC) Pageable pageable) {
        return pagedLeituras.toModel(leituraService.listarPorGeladeira(id, pageable), leituraAssembler);
    }

    @GetMapping("/ocupacao")
    @Operation(summary = "Ranking de ocupacao das geladeiras ativas, da mais cheia para a mais vazia (JPQL)")
    public CollectionModel<OcupacaoResponse> rankingOcupacao() {
        List<OcupacaoResponse> ranking = service.rankingOcupacao();
        return CollectionModel.of(ranking, linkTo(methodOn(GeladeiraController.class).rankingOcupacao()).withSelfRel());
    }

    @GetMapping("/com-espaco")
    @Operation(summary = "Geladeiras ativas que comportam uma doacao de X kg, da com mais espaco para a com menos (JPQL)")
    public CollectionModel<OcupacaoResponse> comEspaco(
            @RequestParam @Positive(message = "kg tem que ser maior que zero") BigDecimal kg) {
        return CollectionModel.of(service.comEspacoLivre(kg),
                linkTo(methodOn(GeladeiraController.class).comEspaco(kg)).withSelfRel());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cadastra uma geladeira")
    public ResponseEntity<EntityModel<GeladeiraResponse>> criar(@Valid @RequestBody GeladeiraRequest req) {
        EntityModel<GeladeiraResponse> model = assembler.toModel(service.criar(req));
        return ResponseEntity.created(model.getRequiredLink("self").toUri()).body(model);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualiza uma geladeira (para tirar da rede, mude o status para INATIVA)")
    public EntityModel<GeladeiraResponse> atualizar(@PathVariable Long id, @Valid @RequestBody GeladeiraRequest req) {
        return assembler.toModel(service.atualizar(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Exclui uma geladeira sem historico de doacoes ou leituras")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
