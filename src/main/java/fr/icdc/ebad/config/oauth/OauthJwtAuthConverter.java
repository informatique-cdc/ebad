package fr.icdc.ebad.config.oauth;

import fr.icdc.ebad.config.properties.EbadProperties;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class OauthJwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    private final EbadProperties ebadProperties;

    public OauthJwtAuthConverter(EbadProperties ebadProperties) {
        this.ebadProperties = ebadProperties;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = Stream.concat(
                jwtGrantedAuthoritiesConverter.convert(jwt).stream(),
                extractResourceRoles(jwt).stream()).collect(Collectors.toSet());
        return new JwtAuthenticationToken(jwt, authorities, getPrincipalClaimName(jwt));
    }

    private String getPrincipalClaimName(Jwt jwt) {
        String claimName = JwtClaimNames.SUB;
        if (ebadProperties.getSecurity().getMappingUser().getLogin() != null) {
            claimName = ebadProperties.getSecurity().getMappingUser().getLogin();
        }
        return jwt.getClaim(claimName);
    }

    private Collection<? extends GrantedAuthority> extractResourceRoles(Jwt jwt) {
//        Map<String, Object> resourceAccess = jwt.getClaim(ebadProperties.getSecurity().getMappingUser().getAuthorities());
        List<String> roles = (List<String>)jwt.getClaimAsMap("realm_access").get("roles");
//        Map<String, Object> resource;
//        Collection<String> resourceRoles;
//        if (resourceAccess == null
//                || (resource = (Map<String, Object>) resourceAccess.get(properties.getResourceId())) == null
//                || (resourceRoles = (Collection<String>) resource.get("roles")) == null) {
//            return Set.of();
//        }
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toSet());
    }
}
