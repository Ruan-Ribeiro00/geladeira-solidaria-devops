package br.com.fiap.geladeira.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Leitura enviada pelo sensor IoT (ESP32 + DHT22 + sensor de porta)")
public record LeituraRequest(
        @NotNull(message = "Informe a geladeira")
        @Schema(example = "3")
        Long geladeiraId,

        @NotNull(message = "A temperatura e obrigatoria")
        @DecimalMin(value = "-40.0", message = "Temperatura fora da faixa do sensor")
        @DecimalMax(value = "80.0", message = "Temperatura fora da faixa do sensor")
        @Schema(example = "9.6")
        BigDecimal temperaturaC,

        @DecimalMin(value = "0.0", message = "Umidade entre 0 e 100")
        @DecimalMax(value = "100.0", message = "Umidade entre 0 e 100")
        @Schema(example = "62.5")
        BigDecimal umidadePct,

        @NotNull(message = "Informe se a porta esta aberta")
        @Schema(example = "false")
        Boolean portaAberta,

        @PastOrPresent(message = "A leitura nao pode estar no futuro")
        @Schema(description = "Se nao vier, usa a hora do servidor")
        LocalDateTime dtLeitura
) {
}
