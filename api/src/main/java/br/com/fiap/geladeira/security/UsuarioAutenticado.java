package br.com.fiap.geladeira.security;

import br.com.fiap.geladeira.model.Papel;
import br.com.fiap.geladeira.model.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/** Usuario logado, do jeito que o Spring Security enxerga. O papel vira a authority ROLE_<PAPEL>. */
public record UsuarioAutenticado(
        Long id,
        String nome,
        String email,
        String senhaHash,
        Papel papel,
        Long doadorId,
        boolean ativo
) implements UserDetails {

    public static UsuarioAutenticado de(Usuario u) {
        return new UsuarioAutenticado(u.getId(), u.getNome(), u.getEmail(), u.getSenha(), u.getPapel(),
                u.getDoador() != null ? u.getDoador().getId() : null, u.isAtivo());
    }

    public boolean is(Papel p) {
        return papel == p;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + papel.name()));
    }

    @Override
    public String getPassword() {
        return senhaHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return ativo;
    }
}
