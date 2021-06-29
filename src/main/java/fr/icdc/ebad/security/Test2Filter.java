package fr.icdc.ebad.security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;


@Component("test2Filter")
public class Test2Filter extends AbstractPreAuthenticatedProcessingFilter implements AuthenticationManager {
    private final EbadUserDetailsService ebadUserDetailsService;

    public Test2Filter(EbadUserDetailsService ebadUserDetailsService) {
        this.ebadUserDetailsService = ebadUserDetailsService;
        super.setAuthenticationManager(this);
    }

    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        System.out.println("TEST2 1");
        return  ebadUserDetailsService.loadUserByUsername("loginTest");
    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        System.out.println("TEST2 3");
        return null;
    }


    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        System.out.println("TEST2 2");
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        if (userPrincipal == null) {
            throw new BadCredentialsException("No user found");
        } else {
            return new PreAuthenticatedAuthenticationToken(userPrincipal, authentication.getCredentials(), userPrincipal.getAuthorities());
        }
    }

}
