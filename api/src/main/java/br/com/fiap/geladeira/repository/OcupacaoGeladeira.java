package br.com.fiap.geladeira.repository;

import java.math.BigDecimal;

/** Projecao das consultas de ocupacao (interface-based projection do Spring Data). */
public interface OcupacaoGeladeira {
    Long getId();
    String getNome();
    String getBairro();
    BigDecimal getCapacidadeKg();
    BigDecimal getOcupadoKg();
}
