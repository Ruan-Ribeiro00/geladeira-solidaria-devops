package br.com.fiap.geladeira.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Le o header "Authorization: Bearer <token>", valida o JWT e coloca o usuario
 * no SecurityContext. Sem token (ou com token invalido) a requisicao segue anonima
 * e quem decide se ela pode passar sao as regras do SecurityConfig.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioDetailsService userDetailsService;

    public JwtAuthFilter(JwtService jwtService, UsuarioDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            jwtService.validarERetornarEmail(header.substring(7).trim()).ifPresent(email -> autenticar(email, request));
        }
        chain.doFilter(request, response);
    }

    private void autenticar(String email, HttpServletRequest request) {
        try {
            UsuarioAutenticado usuario = userDetailsService.loadUserByUsername(email);
            if (!usuario.isEnabled()) {
                return;
            }
            var auth = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (UsernameNotFoundException e) {
            // usuario do token foi removido: segue como anonimo
        }
    }
}
