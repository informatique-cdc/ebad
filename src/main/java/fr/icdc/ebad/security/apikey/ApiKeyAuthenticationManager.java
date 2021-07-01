package fr.icdc.ebad.security.apikey;

import fr.icdc.ebad.domain.User;
import fr.icdc.ebad.security.EbadUserDetailsService;
import fr.icdc.ebad.service.ApiTokenService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class ApiKeyAuthenticationManager implements AuthenticationManager {
    private final EbadUserDetailsService ebadUserDetailsService;
    private final ApiTokenService apiTokenService;

    public ApiKeyAuthenticationManager(EbadUserDetailsService ebadUserDetailsService, ApiTokenService apiTokenService) {
        this.ebadUserDetailsService = ebadUserDetailsService;
        this.apiTokenService = apiTokenService;
    }


    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
            User user = apiTokenService.userFromToken(authentication.getPrincipal().toString());
            if (user == null) {
                throw new UsernameNotFoundException("No user found");
            }
            UserDetails userPrincipal = ebadUserDetailsService.loadUserByUsername(user.getLogin());
            return new PreAuthenticatedAuthenticationToken(userPrincipal, authentication.getCredentials(), userPrincipal.getAuthorities());
    }
}
