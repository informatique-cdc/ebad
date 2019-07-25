package fr.icdc.ebad.security;

import fr.icdc.ebad.domain.User;
import fr.icdc.ebad.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Authenticate a user from the database.
 */
@Component("userDetailsService")
public class EbadUserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EbadUserDetailsService.class);

    private final UserRepository userRepository;

    public EbadUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(final String login) {
        LOGGER.debug("Authenticating {}", login);
        String lowercaseLogin = login.toLowerCase();
        Optional<User> userFromDatabase = userRepository.findOneByLogin(lowercaseLogin);
        return userFromDatabase.map(user -> {
            if (!user.isActivated()) {
                throw new UserNotActivatedException("L'utilisateur " + lowercaseLogin + " n'est pas actif");
            }
            return new org.springframework.security.core.userdetails.User(lowercaseLogin,
                    user.getPassword(),
                    user.getAuthorities().stream()
                            .map(authority -> new SimpleGrantedAuthority(authority.getName()))
                            .collect(Collectors.toList()));
        }).orElseThrow(() -> new UsernameNotFoundException("L'utlisateur " + lowercaseLogin + " n'existe pas"));
    }
}
