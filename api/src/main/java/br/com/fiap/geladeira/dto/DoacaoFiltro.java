package br.com.fiap.geladeira.dto;

import br.com.fiap.geladeira.model.NivelRisco;
import br.com.fiap.geladeira.model.StatusDoacao;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/** Parametros opcionais do filtro dinamico de doacoes (vira uma Specification). */
public record DoacaoFiltro(
        @Parameter(description = "Id da geladeira") Long geladeiraId,
        @Parameter(description = "Id do doador") Long doadorId,
        @Parameter(description = "Id da categoria") Long categoriaId,
        @Parameter(description = "DISPONIVEL, RETIRADA ou DESCARTADA") StatusDoacao status,
        @Parameter(description = "OK, ATENCAO, CRITICO ou VENCIDO") NivelRisco nivelRisco,
        @Parameter(description = "Parte do nome do bairro") String bairro,
        @Parameter(description = "Parte da descricao do alimento") String descricao,
        @Parameter(description = "Validade a partir de (aaaa-mm-dd)")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate validadeDe,
        @Parameter(description = "Validade ate (aaaa-mm-dd)")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate validadeAte,
        @Parameter(description = "true = so alimentos refrigerados") Boolean refrigerado
) {
}
