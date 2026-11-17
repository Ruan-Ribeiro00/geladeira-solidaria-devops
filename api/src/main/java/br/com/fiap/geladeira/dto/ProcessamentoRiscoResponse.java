package br.com.fiap.geladeira.dto;

import java.math.BigDecimal;

/** Resumo da varredura de riscos (RN5) sobre todas as doacoes disponiveis. */
public record ProcessamentoRiscoResponse(
        int doacoesProcessadas,
        int ok,
        int atencao,
        int critico,
        int vencidas,
        BigDecimal kgDescartados,
        int alertasGerados
) {
}
