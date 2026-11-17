package br.com.fiap.geladeira.dto;

import java.time.Instant;

public record TokenResponse(String token, String tipo, Instant expiraEm, UsuarioResponse usuario) {
}
