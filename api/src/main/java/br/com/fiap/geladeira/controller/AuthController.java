package br.com.fiap.geladeira.controller;

import br.com.fiap.geladeira.dto.*;
import br.com.fiap.geladeira.security.UsuarioAutenticado;
import br.com.fiap.geladeira.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticacao", description = "Login com JWT e cadastro publico de doadores")
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Faz login e devolve o token JWT")
    public TokenResponse login(@Valid @RequestBody LoginRequest req) {
        return service.login(req);
    }

    @PostMapping("/registrar")
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirements
    @Operation(summary = "Cadastro publico de doador: cria o doador e a conta (papel DOADOR) e ja devolve o token")
    public TokenResponse registrar(@Valid @RequestBody RegistroRequest req) {
        return service.registrarDoador(req);
    }

    @GetMapping("/me")
    @Operation(summary = "Dados do usuario do token")
    public UsuarioResponse me(@Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return service.me(usuario);
    }
}
