package br.com.fiap.geladeira.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Usuario que acessa a API. A senha fica guardada com BCrypt, nunca em texto puro. */
@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(nullable = false, length = 100)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private Papel papel;

    /** Preenchido so para usuarios DOADOR: o cadastro de doador ligado a esta conta. */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_doador")
    private Doador doador;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "dt_cadastro", nullable = false)
    private LocalDateTime dtCadastro;
}
