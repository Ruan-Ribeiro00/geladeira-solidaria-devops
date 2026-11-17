package br.com.fiap.geladeira.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Aviso gerado para voluntarios e responsaveis (temperatura, validade, lotacao, estoque baixo). */
@Entity
@Table(name = "alerta")
@Getter
@Setter
@NoArgsConstructor
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_geladeira")
    private Geladeira geladeira;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_doacao")
    private Doacao doacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private TipoAlerta tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private NivelAlerta nivel;

    @Column(nullable = false, length = 300)
    private String mensagem;

    @Column(name = "dt_alerta", nullable = false)
    private LocalDateTime dtAlerta;

    @Column(nullable = false)
    private boolean resolvido;

    public static Alerta novo(Geladeira geladeira, Doacao doacao, TipoAlerta tipo, NivelAlerta nivel, String mensagem) {
        Alerta a = new Alerta();
        a.setGeladeira(geladeira);
        a.setDoacao(doacao);
        a.setTipo(tipo);
        a.setNivel(nivel);
        a.setMensagem(mensagem);
        a.setDtAlerta(LocalDateTime.now());
        a.setResolvido(false);
        return a;
    }
}
