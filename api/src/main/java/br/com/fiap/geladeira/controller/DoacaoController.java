package br.com.fiap.geladeira.controller;

import br.com.fiap.geladeira.assembler.DoacaoAssembler;
import br.com.fiap.geladeira.dto.*;
import br.com.fiap.geladeira.security.UsuarioAutenticado;
import br.com.fiap.geladeira.service.DoacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/doacoes")
@Validated
@Tag(name = "Doacoes", description = "Registro, filtro dinamico, distribuicao e riscos dos alimentos doados")
public class DoacaoController {

    private final DoacaoService service;
    private final DoacaoAssembler assembler;
    private final PagedResourcesAssembler<DoacaoResponse> pagedAssembler;

    public DoacaoController(DoacaoService service, DoacaoAssembler assembler,
                            PagedResourcesAssembler<DoacaoResponse> pagedAssembler) {
        this.service = service;
        this.assembler = assembler;
        this.pagedAssembler = pagedAssembler;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Filtro dinamico de doacoes (Specification), paginado e ordenavel",
            description = "Todos os filtros sao opcionais e combinaveis. Ex.: ?nivelRisco=CRITICO&refrigerado=true"
                    + "&sort=dtValidade,asc. Usuarios DOADOR veem so as proprias doacoes.")
    public PagedModel<EntityModel<DoacaoResponse>> listar(
            @ParameterObject DoacaoFiltro filtro,
            @ParameterObject @PageableDefault(size = 10, sort = "dtValidade") Pageable pageable,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return pagedAssembler.toModel(service.listar(filtro, pageable, usuario), assembler);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Busca uma doacao")
    public EntityModel<DoacaoResponse> buscar(@PathVariable Long id,
                                              @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return assembler.toModel(service.buscar(id, usuario));
    }

    @GetMapping("/prioritarias")
    @PreAuthorize("hasAnyRole('ADMIN','VOLUNTARIO')")
    @Operation(summary = "Lista de distribuicao prioritaria (RN3): o que vence nos proximos N dias, por ordem de vencimento")
    public CollectionModel<EntityModel<DoacaoResponse>> prioritarias(
            @RequestParam(defaultValue = "3") @Min(value = 0, message = "dias minimo 0")
            @Max(value = 30, message = "dias maximo 30") int dias) {
        return assembler.toCollectionModel(service.listaPrioritaria(dias))
                .add(linkTo(methodOn(DoacaoController.class).prioritarias(dias)).withSelfRel());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','VOLUNTARIO','DOADOR')")
    @Operation(summary = "Registra uma doacao (RN4)",
            description = "Recusa com 422 se a geladeira nao estiver ATIVA, se a doacao nao couber, se o alimento "
                    + "refrigerado for para uma geladeira quente ou se estiver vencido. No 422, o campo "
                    + "'sugestao' indica a geladeira ativa com mais espaco.")
    public ResponseEntity<EntityModel<DoacaoResponse>> registrar(
            @Valid @RequestBody DoacaoRequest req,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario) {
        EntityModel<DoacaoResponse> model = assembler.toModel(service.registrar(req, usuario));
        return ResponseEntity.created(model.getRequiredLink("self").toUri()).body(model);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','VOLUNTARIO')")
    @Operation(summary = "Corrige uma doacao disponivel (descricao, categoria, quantidade, validade)")
    public EntityModel<DoacaoResponse> atualizar(@PathVariable Long id, @Valid @RequestBody DoacaoAtualizacaoRequest req) {
        return assembler.toModel(service.atualizar(id, req));
    }

    @PatchMapping("/{id}/retirada")
    @PreAuthorize("hasAnyRole('ADMIN','VOLUNTARIO')")
    @Operation(summary = "Marca a doacao como RETIRADA (alimento entregue)")
    public EntityModel<DoacaoResponse> registrarRetirada(@PathVariable Long id) {
        return assembler.toModel(service.registrarRetirada(id));
    }

    @PatchMapping("/{id}/descarte")
    @PreAuthorize("hasAnyRole('ADMIN','VOLUNTARIO')")
    @Operation(summary = "Marca a doacao como DESCARTADA")
    public EntityModel<DoacaoResponse> registrarDescarte(@PathVariable Long id) {
        return assembler.toModel(service.registrarDescarte(id));
    }

    @PostMapping("/processar-riscos")
    @PreAuthorize("hasAnyRole('ADMIN','VOLUNTARIO')")
    @Operation(summary = "Reclassifica o risco de todas as doacoes disponiveis (RN5) e abre os alertas",
            description = "Tambem roda sozinho todo dia as 6h.")
    public ProcessamentoRiscoResponse processarRiscos() {
        return service.processarRiscos();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Apaga uma doacao registrada por engano (so ADMIN)")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
