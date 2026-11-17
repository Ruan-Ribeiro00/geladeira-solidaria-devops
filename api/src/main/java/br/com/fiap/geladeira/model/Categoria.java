package br.com.fiap.geladeira.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** Tipo de alimento. Os refrigerados tem uma temperatura maxima segura. */
@Entity
@Table(name = "categoria")
@Getter
@Setter
@NoArgsConstructor
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String nome;

    @Column(nullable = false)
    private boolean refrigerado;

    @Column(name = "temp_max_c", precision = 4, scale = 1)
    private BigDecimal tempMaxC;
}
