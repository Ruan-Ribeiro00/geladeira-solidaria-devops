package br.com.fiap.geladeira.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Geladeira Solidaria Inteligente - API")
                        .version("1.0.0")
                        .description("""
                                Rede de geladeiras comunitarias de doacao de alimentos monitoradas por sensores IoT.
                                Global Solution 2026/2 (CitizenTech) - FIAP.

                                **Como testar:** chame `POST /auth/login` com um dos usuarios de teste,
                                copie o `token` e clique em **Authorize** (cadeado) para colar.

                                | Papel | E-mail | Senha |
                                |---|---|---|
                                | ADMIN | admin@geladeira.test | Admin@123 |
                                | VOLUNTARIO | voluntario@geladeira.test | Voluntario@123 |
                                | DOADOR | doador@geladeira.test | Doador@123 |
                                | SENSOR | sensor@geladeira.test | Sensor@123 |
                                """)
                        .contact(new Contact().name("Ruan Ribeiro - RM 567693")))
                .components(new Components().addSecuritySchemes(BEARER, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER));
    }
}
