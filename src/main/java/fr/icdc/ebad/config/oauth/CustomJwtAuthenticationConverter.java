package fr.icdc.ebad.config.oauth;

import fr.icdc.ebad.config.properties.EbadProperties;
import fr.icdc.ebad.domain.Authority;
import fr.icdc.ebad.domain.User;
import fr.icdc.ebad.repository.AuthorityRepository;
import fr.icdc.ebad.repository.UserRepository;
import fr.icdc.ebad.security.EbadUserDetailsService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.*;
import java.util.stream.Collectors;

public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    private final EbadUserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;
    private final EbadProperties ebadProperties;

    public CustomJwtAuthenticationConverter(EbadUserDetailsService userDetailsService, UserRepository userRepository, AuthorityRepository authorityRepository, EbadProperties ebadProperties) {
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
        this.authorityRepository = authorityRepository;
        this.ebadProperties = ebadProperties;
    }


    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        String principalClaimName = "preferred_username"; // Changez selon vos besoins
        String principal = jwt.getClaimAsString(principalClaimName);

//        String authoritiesString = jwt.getClaim(ebadProperties.getSecurity().getMappingUser().getAuthorities());
        String login = jwt.getClaim(ebadProperties.getSecurity().getMappingUser().getLogin());
        String firstname = jwt.getClaim(ebadProperties.getSecurity().getMappingUser().getFirstname());
        String lastname = jwt.getClaim(ebadProperties.getSecurity().getMappingUser().getLastname());
        String email = jwt.getClaim(ebadProperties.getSecurity().getMappingUser().getEmail());
        Optional<User> userOptional = userRepository.findOneByLogin(login);

        Set<Authority> authoritiesSet = new HashSet<>();

        for (GrantedAuthority authority : authorities) {
            Optional<Authority> authority1 = authorityRepository.findById(authority.getAuthority());
            authority1.ifPresent(authoritiesSet::add);
        }


        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setAuthorities(authoritiesSet);
            user.setFirstName(firstname);
            user.setLastName(lastname);
            user.setEmail(email);
            userRepository.save(user);
        } else {
            User newUser = new User();
            newUser.setAuthorities(authoritiesSet);
            newUser.setLogin(login);
            newUser.setPassword("NOPASSWORD");
            newUser.setFirstName(firstname);
            newUser.setLastName(lastname);
            newUser.setEmail(email);
            newUser.setLangKey("FR_fr");
            newUser.setActivated(true);
            newUser.setCreatedBy("OAUTH2");
            userRepository.save(newUser);
        }

//         userDetailsService.loadUserByUsername(login);
        // Créez un principal personnalisé si nécessaire
        UserDetails customUserPrincipal = userDetailsService.loadUserByUsername(login);

//        return new JwtAuthenticationToken(jwt, authorities, customUserPrincipal.getUsername());
        return new UsernamePasswordAuthenticationToken(customUserPrincipal, jwt, authorities);
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Collection<GrantedAuthority> authorities = grantedAuthoritiesConverter.convert(jwt);

        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        List<String> authoritiesList = jwt.getClaim(ebadProperties.getSecurity().getMappingUser().getAuthorities());

        for (String authority : authoritiesList) {
            grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + authority));
        }
        authorities.addAll(grantedAuthorities);


        return authorities;
    }
}
