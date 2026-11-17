package br.com.fiap.geladeira.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Contas de ocupacao usadas em mais de um service. */
final class Calculos {

    private static final BigDecimal CEM = BigDecimal.valueOf(100);

    private Calculos() {
    }

    static BigDecimal percentual(BigDecimal parte, BigDecimal total) {
        if (total == null || total.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return parte.multiply(CEM).divide(total, 1, RoundingMode.HALF_UP);
    }

    static BigDecimal livre(BigDecimal capacidade, BigDecimal ocupado) {
        BigDecimal livre = capacidade.subtract(ocupado);
        return livre.signum() < 0 ? BigDecimal.ZERO : livre;
    }
}
