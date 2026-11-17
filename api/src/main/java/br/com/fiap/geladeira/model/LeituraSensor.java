package br.com.fiap.geladeira.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Leitura enviada pelo sensor IoT da geladeira (temperatura, umidade e porta). */
@Entity
@Table(name = "leitura_sensor")
@Getter
@Setter
@NoArgsConstructor
public class LeituraSensor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_geladeira")
    private Geladeira geladeira;

    @Column(name = "dt_leitura", nullable = false)
    private LocalDateTime dtLeitura;

    @Column(name = "temperatura_c", nullable = false, precision = 4, scale = 1)
    private BigDecimal temperaturaC;

    @Column(name = "umidade_pct", precision = 5, scale = 2)
    private BigDecimal umidadePct;

    @Column(name = "porta_aberta", nullable = false)
    private boolean portaAberta;
}
