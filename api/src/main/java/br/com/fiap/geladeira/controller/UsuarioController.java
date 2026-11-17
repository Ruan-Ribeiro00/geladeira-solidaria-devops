package br.com.fiap.geladeira.controller;

import br.com.fiap.geladeira.dto.UsuarioRequest;
import br.com.fiap.geladeira.dto.UsuarioResponse;
import br.com.fiap.geladeira.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Usuarios", description = "Contas internas: ADMIN, VOLUNTARIO e SENSOR (so ADMIN)")
public class UsuarioController {

    private final AuthService service;

    public UsuarioController(AuthService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista as contas (paginado)")
    public Page<UsuarioResponse> listar(@ParameterObject @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        return service.listarUsuarios(pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria uma conta interna (ex.: um novo voluntario ou o usuario de um sensor)")
    public UsuarioResponse criar(@Valid @RequestBody UsuarioRequest req) {
        return service.criarUsuarioInterno(req);
    }
}
