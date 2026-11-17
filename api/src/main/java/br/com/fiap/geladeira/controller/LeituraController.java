package br.com.fiap.geladeira.controller;

import br.com.fiap.geladeira.assembler.LeituraAssembler;
import br.com.fiap.geladeira.dto.LeituraRequest;
import br.com.fiap.geladeira.dto.LeituraResponse;
import br.com.fiap.geladeira.service.LeituraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leituras")
@Tag(name = "Leituras do sensor", description = "Entrada de dados do dispositivo IoT de cada geladeira")
public class LeituraController {

    private final LeituraService service;
    private final LeituraAssembler assembler;

    public LeituraController(LeituraService service, LeituraAssembler assembler) {
        this.service = service;
        this.assembler = assembler;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('SENSOR','ADMIN')")
    @Operation(summary = "Recebe uma leitura do sensor. Acima de 8 C abre alerta de TEMPERATURA (sem duplicar)")
    public EntityModel<LeituraResponse> registrar(@Valid @RequestBody LeituraRequest req) {
        return assembler.toModel(service.registrar(req));
    }
}
