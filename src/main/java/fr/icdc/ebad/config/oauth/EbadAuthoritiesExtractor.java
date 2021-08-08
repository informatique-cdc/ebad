package fr.icdc.ebad.config.oauth;

import fr.icdc.ebad.config.properties.EbadProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.AuthoritiesExtractor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EbadAuthoritiesExtractor implements AuthoritiesExtractor {
    private final EbadProperties ebadProperties;

    public EbadAuthoritiesExtractor(EbadProperties ebadProperties) {
        this.ebadProperties = ebadProperties;
    }

    @Override
    public List<GrantedAuthority> extractAuthorities(Map<String, Object> map) {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        List<String> authoritiesString = (List<String>) map.get(ebadProperties.getSecurity().getMappingUser().getAuthorities());
        for (String authority : authoritiesString) {
            grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + authority));
        }
        return grantedAuthorities;
    }
}
