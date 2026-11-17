package br.com.fiap.geladeira.service;

import br.com.fiap.geladeira.dto.*;
import br.com.fiap.geladeira.exception.ConflitoException;
import br.com.fiap.geladeira.model.Doador;
import br.com.fiap.geladeira.model.Papel;
import br.com.fiap.geladeira.model.Usuario;
import br.com.fiap.geladeira.repository.UsuarioRepository;
import br.com.fiap.geladeira.security.JwtService;
import br.com.fiap.geladeira.security.UsuarioAutenticado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final DoadorService doadorService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService,
                       UsuarioRepository usuarioRepository, DoadorService doadorService,
                       PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.doadorService = doadorService;
        this.passwordEncoder = passwordEncoder;
    }

    /** Confere e-mail e senha (BCrypt) e devolve um JWT. */
    public TokenResponse login(LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email().trim().toLowerCase(), req.senha()));
        UsuarioAutenticado usuario = (UsuarioAutenticado) auth.getPrincipal();
        return emitirToken(usuario);
    }

    /** Cadastro publico: cria o doador e a conta com papel DOADOR, ja devolvendo o token. */
    @Transactional
    public TokenResponse registrarDoador(RegistroRequest req) {
        String email = req.email().trim().toLowerCase();
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflitoException("Ja existe uma conta com o e-mail " + email);
        }
        Doador doador = doadorService.criarEntidade(new DoadorRequest(req.nome(), req.tipo(), req.telefone(), email));
        Usuario u = novoUsuario(req.nome(), email, req.senha(), Papel.DOADOR);
        u.setDoador(doador);
        usuarioRepository.save(u);
        return emitirToken(UsuarioAutenticado.de(u));
    }

    /** ADMIN cria contas internas: outros admins, voluntarios ou o usuario do sensor. */
    @Transactional
    public UsuarioResponse criarUsuarioInterno(UsuarioRequest req) {
        if (req.papel() == Papel.DOADOR) {
            throw new br.com.fiap.geladeira.exception.RegraNegocioException(
                    "Contas de doador sao criadas pelo cadastro publico (POST /auth/registrar)");
        }
        String email = req.email().trim().toLowerCase();
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflitoException("Ja existe uma conta com o e-mail " + email);
        }
        Usuario u = usuarioRepository.save(novoUsuario(req.nome(), email, req.senha(), req.papel()));
        return toResponse(u);
    }

    @Transactional(readOnly = true)
    public Page<UsuarioResponse> listarUsuarios(Pageable pageable) {
        return usuarioRepository.findAll(pageable).map(AuthService::toResponse);
    }

    public UsuarioResponse me(UsuarioAutenticado u) {
        return new UsuarioResponse(u.id(), u.nome(), u.email(), u.papel(), u.doadorId());
    }

    private Usuario novoUsuario(String nome, String email, String senha, Papel papel) {
        Usuario u = new Usuario();
        u.setNome(nome.trim());
        u.setEmail(email);
        u.setSenha(passwordEncoder.encode(senha));
        u.setPapel(papel);
        u.setAtivo(true);
        u.setDtCadastro(LocalDateTime.now());
        return u;
    }

    private TokenResponse emitirToken(UsuarioAutenticado u) {
        JwtService.TokenEmitido token = jwtService.gerar(u);
        return new TokenResponse(token.valor(), "Bearer", token.expiraEm(), me(u));
    }

    private static UsuarioResponse toResponse(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNome(), u.getEmail(), u.getPapel(),
                u.getDoador() != null ? u.getDoador().getId() : null);
    }
}
