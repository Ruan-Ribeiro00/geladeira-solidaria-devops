package br.com.fiap.geladeira.security;

import br.com.fiap.geladeira.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

/** Gera e valida os tokens JWT (HMAC-SHA256). */
@Service
public class JwtService {

    private static final String EMISSOR = "geladeira-solidaria-api";

    private final SecretKey chave;
    private final long minutosValidade;

    public JwtService(JwtProperties props) {
        byte[] bytes = props.secret().getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("app.jwt.secret (JWT_SECRET) precisa ter pelo menos 32 caracteres");
        }
        this.chave = Keys.hmacShaKeyFor(bytes);
        this.minutosValidade = props.expirationMinutes();
    }

    public record TokenEmitido(String valor, Instant expiraEm) {
    }

    public TokenEmitido gerar(UsuarioAutenticado usuario) {
        Instant agora = Instant.now();
        Instant expira = agora.plus(minutosValidade, ChronoUnit.MINUTES);
        String token = Jwts.builder()
                .issuer(EMISSOR)
                .subject(usuario.email())
                .claim("uid", usuario.id())
                .claim("papel", usuario.papel().name())
                .issuedAt(Date.from(agora))
                .expiration(Date.from(expira))
                .signWith(chave)
                .compact();
        return new TokenEmitido(token, expira);
    }

    /** Devolve o e-mail (subject) se o token for valido, assinado por nos e nao estiver expirado. */
    public Optional<String> validarERetornarEmail(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(chave)
                    .requireIssuer(EMISSOR)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.ofNullable(claims.getSubject());
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
