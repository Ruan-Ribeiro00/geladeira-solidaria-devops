package br.com.fiap.geladeira.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/** Pessoa (PF) ou comercio (PJ) que doa alimentos. */
@Entity
@Table(name = "doador")
@Getter
@Setter
@NoArgsConstructor
public class Doador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 2)
    private TipoDoador tipo;

    @Column(length = 20)
    private String telefone;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(name = "dt_cadastro", nullable = false)
    private LocalDate dtCadastro;
}
