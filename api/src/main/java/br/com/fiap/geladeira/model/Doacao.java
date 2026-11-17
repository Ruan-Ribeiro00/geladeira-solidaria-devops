package br.com.fiap.geladeira.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Alimento doado e deixado numa geladeira. */
@Entity
@Table(name = "doacao")
@Getter
@Setter
@NoArgsConstructor
public class Doacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_geladeira")
    private Geladeira geladeira;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_doador")
    private Doador doador;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_categoria")
    private Categoria categoria;

    @Column(nullable = false, length = 120)
    private String descricao;

    @Column(name = "quantidade_kg", nullable = false, precision = 8, scale = 2)
    private BigDecimal quantidadeKg;

    @Column(name = "dt_doacao", nullable = false)
    private LocalDateTime dtDoacao;

    @Column(name = "dt_validade", nullable = false)
    private LocalDate dtValidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private StatusDoacao status;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_risco", nullable = false, length = 10)
    private NivelRisco nivelRisco;

    public boolean isDisponivel() {
        return status == StatusDoacao.DISPONIVEL;
    }
}
