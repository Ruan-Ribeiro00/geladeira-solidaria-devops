package br.com.fiap.geladeira.config;

import br.com.fiap.geladeira.security.JwtAuthFilter;
import br.com.fiap.geladeira.security.RespostasSeguranca;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Seguranca stateless com JWT.
 * Aqui ficam as rotas publicas; as regras por papel ficam em cada endpoint com @PreAuthorize,
 * para que quem le o controller veja na hora quem pode chamar o que.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] ROTAS_PUBLICAS = {
            "/auth/login", "/auth/registrar",
            "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
            "/h2-console/**", "/error"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter,
                                                   RespostasSeguranca respostas) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)              // API stateless com token, sem cookie de sessao
                .cors(cors -> {})                                   // usa o corsConfigurationSource abaixo
                .headers(h -> h.frameOptions(f -> f.sameOrigin()))  // libera o console do H2 no perfil h2
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(ROTAS_PUBLICAS).permitAll()
                        .requestMatchers(EndpointRequest.to(HealthEndpoint.class, InfoEndpoint.class)).permitAll()
                        .requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole("ADMIN")
                        // o mapa do app funciona sem login: qualquer pessoa ve onde ha geladeira e o que tem nela
                        .requestMatchers(HttpMethod.GET, "/api/geladeiras", "/api/geladeiras/*",
                                "/api/geladeiras/*/situacao", "/api/geladeiras/ocupacao",
                                "/api/geladeiras/com-espaco", "/api/categorias/**").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(respostas.naoAutenticado())
                        .accessDeniedHandler(respostas.acessoNegado()))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /** CORS para o app mobile (Flutter web/emulador) e qualquer front que consuma a API. */
    @Bean
    public CorsConfigurationSource corsConfigurationSource(CorsProperties props) {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOriginPatterns(props.allowedOrigins());
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        cfg.setExposedHeaders(List.of("Location"));
        cfg.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
