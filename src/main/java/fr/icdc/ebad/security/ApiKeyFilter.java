package fr.icdc.ebad.security;

import fr.icdc.ebad.domain.User;
import fr.icdc.ebad.service.ApiTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.authentication.www.BasicAuthenticationConverter;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;


@Component
public class ApiKeyFilter extends AbstractPreAuthenticatedProcessingFilter implements AuthenticationManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiKeyFilter.class);

    private final EbadUserDetailsService ebadUserDetailsService;
    private final ApiTokenService apiTokenService;

    public ApiKeyFilter(EbadUserDetailsService ebadUserDetailsService, ApiTokenService apiTokenService) {
        this.ebadUserDetailsService = ebadUserDetailsService;
        this.apiTokenService = apiTokenService;
        super.setAuthenticationManager(this);
    }

    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        BasicAuthenticationConverter basicAuthConverter = new BasicAuthenticationConverter();
        UsernamePasswordAuthenticationToken basicToken = basicAuthConverter.convert(request);

        System.out.println("TEST2 1");
        try {
            User user = apiTokenService.userFromToken(basicToken.getPrincipal().toString(), basicToken.getCredentials().toString());
            if(user == null){
                return null;
            }
            return ebadUserDetailsService.loadUserByUsername(user.getLogin());
        }catch (UserNotActivatedException | UsernameNotFoundException e){
            LOGGER.error("Error when trying to log user", e);
            return null;
        }
    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        return null;
    }


    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        if (userPrincipal == null) {
            throw new BadCredentialsException("No user found");
        } else {
            return new PreAuthenticatedAuthenticationToken(userPrincipal, authentication.getCredentials(), userPrincipal.getAuthorities());
        }
    }

}
