package fr.icdc.ebad.config.apidoc;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI acustomOpenAPI() {
        String securitySchemeJwt = "jwt";
        String securitySchemeApiKey = "ebad-api-key";
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeApiKey))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeJwt))
                .components(
                        new Components()
                                .addSecuritySchemes(securitySchemeJwt,
                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                )
                                .addSecuritySchemes(securitySchemeApiKey,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .scheme(securitySchemeApiKey)
                                        .in(SecurityScheme.In.HEADER)
                        )
                )
                .info(new Info().version("2.7.0").title("EBAD API").description(
                        "This documentation describe EBAD API."));
    }

//    @Bean
//    public OpenAPI customOpenAPI() {
//        return new OpenAPI()
//                .components(new Components())
//                .info(new Info().title("EBAD API").description(
//                        "This documentation describe EBAD API."));
//    }
}
