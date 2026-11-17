package br.com.fiap.geladeira.dto;

import br.com.fiap.geladeira.model.NivelAlerta;
import br.com.fiap.geladeira.model.TipoAlerta;
import org.springframework.hateoas.server.core.Relation;

import java.time.LocalDateTime;

@Relation(itemRelation = "alerta", collectionRelation = "alertas")
public record AlertaResponse(
        Long id,
        Long geladeiraId,
        String geladeiraNome,
        Long doacaoId,
        TipoAlerta tipo,
        NivelAlerta nivel,
        String mensagem,
        LocalDateTime dtAlerta,
        boolean resolvido
) {
}
