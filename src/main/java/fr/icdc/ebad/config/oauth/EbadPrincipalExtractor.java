package fr.icdc.ebad.config.oauth;

import fr.icdc.ebad.config.properties.EbadProperties;
import fr.icdc.ebad.domain.Authority;
import fr.icdc.ebad.domain.User;
import fr.icdc.ebad.repository.AuthorityRepository;
import fr.icdc.ebad.repository.UserRepository;
import fr.icdc.ebad.security.EbadUserDetailsService;
import org.springframework.boot.autoconfigure.security.oauth2.resource.PrincipalExtractor;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

public class EbadPrincipalExtractor implements PrincipalExtractor {
    private final EbadUserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;
    private final EbadProperties ebadProperties;

    public EbadPrincipalExtractor(EbadUserDetailsService userDetailsService, UserRepository userRepository, AuthorityRepository authorityRepository, EbadProperties ebadProperties) {
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
        this.authorityRepository = authorityRepository;
        this.ebadProperties = ebadProperties;
    }

    @Override
    @Transactional
    public Object extractPrincipal(Map<String, Object> map) {
        List<String> authoritiesString = (List<String>) map.get(ebadProperties.getSecurity().getMappingUser().getAuthorities());
        String login = (String) map.get(ebadProperties.getSecurity().getMappingUser().getLogin());
        String firstname = (String) map.get(ebadProperties.getSecurity().getMappingUser().getFirstname());
        String lastname = (String) map.get(ebadProperties.getSecurity().getMappingUser().getLastname());
        String email = (String) map.get(ebadProperties.getSecurity().getMappingUser().getEmail());
        Optional<User> userOptional = userRepository.findOneByLogin(login);

        Set<Authority> authorities = new HashSet<>();

        for (String authority : authoritiesString) {
            authorities.add(authorityRepository.getById("ROLE_" + authority));
        }

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setAuthorities(authorities);
            user.setFirstName(firstname);
            user.setLastName(lastname);
            user.setEmail(email);
            userRepository.save(user);
        } else {
            User newUser = new User();
            newUser.setAuthorities(authorities);
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

        return userDetailsService.loadUserByUsername(login);
    }
}
