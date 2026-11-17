package br.com.fiap.geladeira.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SituacaoGeladeiraResponse(
        Long geladeiraId,
        String nome,
        SituacaoGeladeira situacao,
        String recomendacao,
        BigDecimal temperaturaMediaC,
        LocalDateTime ultimaLeitura,
        Boolean portaAberta,
        BigDecimal ocupacaoPct
) {
}
