package br.com.fiap.geladeira.dto;

import br.com.fiap.geladeira.model.NivelRisco;
import br.com.fiap.geladeira.model.StatusDoacao;
import org.springframework.hateoas.server.core.Relation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Relation(itemRelation = "doacao", collectionRelation = "doacoes")
public record DoacaoResponse(
        Long id,
        String descricao,
        BigDecimal quantidadeKg,
        LocalDateTime dtDoacao,
        LocalDate dtValidade,
        long diasParaVencer,
        StatusDoacao status,
        NivelRisco nivelRisco,
        Long geladeiraId,
        String geladeiraNome,
        String bairro,
        Long doadorId,
        String doadorNome,
        Long categoriaId,
        String categoriaNome,
        String aviso
) {
    public DoacaoResponse comAviso(String novoAviso) {
        return new DoacaoResponse(id, descricao, quantidadeKg, dtDoacao, dtValidade, diasParaVencer, status,
                nivelRisco, geladeiraId, geladeiraNome, bairro, doadorId, doadorNome, categoriaId, categoriaNome,
                novoAviso);
    }
}
