package fr.icdc.ebad.config.apidoc;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
public class OpenApiConfig {

    private final Optional<BuildProperties> buildProperties;

    public OpenApiConfig(Optional<BuildProperties> buildProperties) {
        this.buildProperties = buildProperties;
    }

    @Bean
    public OpenAPI acustomOpenAPI() {
        String version = "unknown";
        if (buildProperties.isPresent()) {
            version = buildProperties.get().getVersion();
        }
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
                                        .name(securitySchemeApiKey)
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                        )
                )
                .info(new Info().version(version).title("EBAD API").description("This documentation describe EBAD API."));
    }
}
