//package fr.icdc.ebad.config.oauth;
//
//import fr.icdc.ebad.config.properties.EbadProperties;
//import org.springframework.boot.autoconfigure.security.oauth2.resource.AuthoritiesExtractor;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//public class EbadAuthoritiesExtractor implements AuthoritiesExtractor {
//    private final EbadProperties ebadProperties;
//
//    public EbadAuthoritiesExtractor(EbadProperties ebadProperties) {
//        this.ebadProperties = ebadProperties;
//    }
//
//    @Override
//    public List<GrantedAuthority> extractAuthorities(Map<String, Object> map) {
//        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
//        String authoritiesString = (String) map.get(ebadProperties.getSecurity().getMappingUser().getAuthorities());
//        authoritiesString = authoritiesString.replaceAll(" ", "");
//        authoritiesString = authoritiesString.replace("[", "");
//        authoritiesString = authoritiesString.replace("]", "");
//        authoritiesString = authoritiesString.replace(ebadProperties.getApplicationIdentifier(), "");
//        for (String authority : authoritiesString.split(",")) {
//            grantedAuthorities.add(new SimpleGrantedAuthority("ROLE" + authority));
//        }
//        return grantedAuthorities;
//    }
//}
