package fr.icdc.ebad.security.permission;

import fr.icdc.ebad.domain.AccreditationRequest;
import fr.icdc.ebad.domain.Application;
import fr.icdc.ebad.repository.AccreditationRequestRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class PermissionAccreditationRequestTest {
    @Mock
    private PermissionApplication permissionApplication;

    @Mock
    private AccreditationRequestRepository accreditationRequestRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private PermissionAccreditationRequest permissionAccreditationRequest;

    @Mock
    private UserDetails userDetails;

    @Test
    public void canAcceptAccreditationRequestAdmin() {
        Collection authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        when(authentication.getAuthorities()).thenReturn(authorities);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        assertTrue(permissionAccreditationRequest.canAcceptAccreditationRequest(1L, userDetails));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void canAcceptAccreditationRequestUserNoAccreditation() {
        Collection authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        when(authentication.getAuthorities()).thenReturn(authorities);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(accreditationRequestRepository.findById(eq(1L))).thenReturn(Optional.empty());
        assertFalse(permissionAccreditationRequest.canAcceptAccreditationRequest(1L, userDetails));
    }

    @Test
    public void cantAcceptAccreditationRequestUser() {
        Collection authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        when(authentication.getAuthorities()).thenReturn(authorities);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        AccreditationRequest accreditationRequest = AccreditationRequest
                .builder()
                .id(1L)
                .application(Application.builder().id(2L).build())
                .build();
        when(accreditationRequestRepository.findById(eq(1L))).thenReturn(Optional.of(accreditationRequest));
        when(permissionApplication.canWrite(eq(2L), eq(userDetails))).thenReturn(false);
        assertFalse(permissionAccreditationRequest.canAcceptAccreditationRequest(1L, userDetails));
    }

    @Test
    public void canAcceptAccreditationRequestUser() {
        Collection authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        when(authentication.getAuthorities()).thenReturn(authorities);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);


        AccreditationRequest accreditationRequest = AccreditationRequest
                .builder()
                .id(1L)
                .application(Application.builder().id(2L).build())
                .build();
        when(accreditationRequestRepository.findById(eq(1L))).thenReturn(Optional.of(accreditationRequest));
        when(permissionApplication.canWrite(eq(2L), eq(userDetails))).thenReturn(true);
        assertTrue(permissionAccreditationRequest.canAcceptAccreditationRequest(1L, userDetails));
    }
}
