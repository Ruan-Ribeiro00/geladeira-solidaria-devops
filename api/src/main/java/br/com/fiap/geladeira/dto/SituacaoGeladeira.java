package br.com.fiap.geladeira.dto;

/** Resultado da regra RN1, na ordem de prioridade em que e avaliada. */
public enum SituacaoGeladeira {
    FORA_DE_OPERACAO,
    SENSOR_SEM_SINAL,
    TEMPERATURA_FORA_DO_LIMITE,
    LOTADA,
    ESTOQUE_BAIXO,
    NORMAL
}
