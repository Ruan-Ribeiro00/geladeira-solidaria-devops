package br.com.fiap.geladeira.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Ponto de doacao: uma geladeira comunitaria instalada num espaco publico. */
@Entity
@Table(name = "geladeira")
@Getter
@Setter
@NoArgsConstructor
public class Geladeira {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 150)
    private String endereco;

    @Column(nullable = false, length = 60)
    private String bairro;

    @Column(nullable = false, length = 60)
    private String cidade;

    @Column(precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(name = "capacidade_kg", nullable = false, precision = 8, scale = 2)
    private BigDecimal capacidadeKg;

    @Column(nullable = false, length = 100)
    private String responsavel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private StatusGeladeira status;

    @Column(name = "dt_instalacao", nullable = false)
    private LocalDate dtInstalacao;

    @OneToMany(mappedBy = "geladeira")
    private List<Doacao> doacoes = new ArrayList<>();

    @OneToMany(mappedBy = "geladeira")
    private List<LeituraSensor> leituras = new ArrayList<>();

    public boolean isAtiva() {
        return status == StatusGeladeira.ATIVA;
    }
}
