package fr.icdc.ebad.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "ebad", ignoreUnknownFields = false)
public class EbadProperties {
    private EbadProperties.SecurityProperties security = new EbadProperties.SecurityProperties();
    private EbadProperties.SshProperties ssh = new EbadProperties.SshProperties();
    private EbadProperties.PluginProperties plugin = new EbadProperties.PluginProperties();

    @Getter
    @Setter
    public static class PluginProperties {
        private String path = "plugins";
    }

    @Getter
    @Setter
    public static class SecurityProperties {
        private EbadProperties.SecurityProperties.RememberMeProperties rememberme = new EbadProperties.SecurityProperties.RememberMeProperties();
        private EbadProperties.SecurityProperties.AuthenticationProperties authentication = new EbadProperties.SecurityProperties.AuthenticationProperties();
        private EbadProperties.SecurityProperties.MappingUserProperties mappingUser = new EbadProperties.SecurityProperties.MappingUserProperties();

        @Getter
        @Setter
        public static class RememberMeProperties {
            @NotNull
            private String key;
        }

        @Getter
        @Setter
        public static class AuthenticationProperties {
            private EbadProperties.SecurityProperties.AuthenticationProperties.JwtProperties jwt = new EbadProperties.SecurityProperties.AuthenticationProperties.JwtProperties();

            @Getter
            @Setter
            public static class JwtProperties {
                @NotNull
                private String secret;
                private long tokenValidityInSeconds = 1800L;
                private long tokenValidityInSecondsForRememberMe = 2592000L;
            }
        }

        @Getter
        @Setter
        public static class MappingUserProperties {
            private String authorities = "authorities";
            private String login = "sub";
            private String firstname = "firstname";
            private String lastname = "lastname";
            private String email = "email";
        }

    }

    @Getter
    @Setter
    public static class SshProperties {
        @NotNull
        private String login;
        private int port = 22;
        @NotNull
        private String privateKeyPath;
        private String privateKeyPassphrase;

    }
}
