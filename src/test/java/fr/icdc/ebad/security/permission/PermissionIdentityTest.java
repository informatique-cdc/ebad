package fr.icdc.ebad.security.permission;

import fr.icdc.ebad.domain.Application;
import fr.icdc.ebad.domain.Identity;
import fr.icdc.ebad.repository.IdentityRepository;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import static fr.icdc.ebad.config.Constants.ROLE_ADMIN;
import static fr.icdc.ebad.config.Constants.ROLE_USER;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class PermissionIdentityTest {
    @Mock
    UserDetails userDetails;

    @InjectMocks
    private PermissionIdentity permissionIdentity;

    @Mock
    private PermissionApplication permissionApplication;

    @Mock
    private IdentityRepository identityRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @Test
    public void canReadByApplication() {
        Collection authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(ROLE_ADMIN));
        when(authentication.getAuthorities()).thenReturn(authorities);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        boolean result1 = permissionIdentity.canReadByApplication(null, userDetails);

        assertTrue(result1);
        verify(permissionApplication, never()).canWrite(eq(1L), eq(userDetails));
    }

    @Test
    public void canReadByApplication2() {
        Collection authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(ROLE_USER));
        when(authentication.getAuthorities()).thenReturn(authorities);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        boolean result1 = permissionIdentity.canReadByApplication(null, userDetails);

        assertTrue(result1);
        verify(permissionApplication, never()).canWrite(eq(1L), eq(userDetails));
    }

    @Test
    public void canReadByApplication3() {
        when(permissionApplication.canWrite(eq(1L), eq(userDetails))).thenReturn(true);
        when(permissionApplication.canWrite(eq(2L), eq(userDetails))).thenReturn(false);

        boolean result1 = permissionIdentity.canReadByApplication(1L, userDetails);
        assertTrue(result1);

        boolean result2 = permissionIdentity.canReadByApplication(2L, userDetails);
        assertFalse(result2);

        verify(permissionApplication, times(1)).canWrite(eq(1L), eq(userDetails));
        verify(permissionApplication, times(1)).canWrite(eq(2L), eq(userDetails));
    }

    @Test
    public void canRead1() {
        boolean result1 = permissionIdentity.canRead(null, userDetails);
        assertFalse(result1);
    }

    @Test
    public void canRead2() {
        when(identityRepository.findById(eq(1L))).thenReturn(Optional.empty());
        boolean result1 = permissionIdentity.canRead(1L, userDetails);
        assertFalse(result1);
    }

    @Test
    public void canRead3() {
        Collection authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(ROLE_USER));
        when(authentication.getAuthorities()).thenReturn(authorities);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        Identity identity = Identity.builder().build();
        when(identityRepository.findById(eq(1L))).thenReturn(Optional.of(identity));

        boolean result1 = permissionIdentity.canRead(1L, userDetails);
        assertTrue(result1);
    }

    @Test
    public void canRead4() {
        Collection authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(ROLE_ADMIN));
        when(authentication.getAuthorities()).thenReturn(authorities);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        Identity identity = Identity.builder().build();
        when(identityRepository.findById(eq(1L))).thenReturn(Optional.of(identity));

        boolean result1 = permissionIdentity.canRead(1L, userDetails);
        assertTrue(result1);
    }

    @Test
    public void canRead5() {
        Application application = Application.builder().id(2L).build();
        Identity identity = Identity.builder().availableApplication(application).build();
        when(identityRepository.findById(eq(1L))).thenReturn(Optional.of(identity));
        when(permissionApplication.canWrite(eq(2L), eq(userDetails))).thenReturn(true);

        boolean result1 = permissionIdentity.canRead(1L, userDetails);
        assertTrue(result1);
        verify(permissionApplication, times(1)).canWrite(eq(2L), eq(userDetails));

    }

    @Test
    public void canWriteByApplication() {
        Collection authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(ROLE_ADMIN));
        when(authentication.getAuthorities()).thenReturn(authorities);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        boolean result1 = permissionIdentity.canWriteByApplication(null, userDetails);
        assertTrue(result1);
    }

    @Test
    public void canWriteByApplication2() {
        Collection authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(ROLE_USER));
        when(authentication.getAuthorities()).thenReturn(authorities);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        boolean result1 = permissionIdentity.canWriteByApplication(null, userDetails);
        assertFalse(result1);
    }

    @Test
    public void canWriteByApplication3() {
        when(permissionApplication.canWrite(eq(1L), eq(userDetails))).thenReturn(true);
        when(permissionApplication.canWrite(eq(2L), eq(userDetails))).thenReturn(false);
        boolean result1 = permissionIdentity.canWriteByApplication(1L, userDetails);
        assertTrue(result1);

        boolean result2 = permissionIdentity.canWriteByApplication(2L, userDetails);
        assertFalse(result2);

        verify(permissionApplication, times(1)).canWrite(eq(1L), eq(userDetails));
        verify(permissionApplication, times(1)).canWrite(eq(2L), eq(userDetails));

    }

    @Test
    public void canWrite1() {
        boolean result1 = permissionIdentity.canWrite(null, userDetails);
        assertFalse(result1);
    }

    @Test
    public void canWrite2() {
        when(identityRepository.findById(eq(1L))).thenReturn(Optional.empty());
        boolean result1 = permissionIdentity.canWrite(1L, userDetails);
        assertFalse(result1);
    }

    @Test
    public void canWrite3() {
        Collection authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(ROLE_USER));
        when(authentication.getAuthorities()).thenReturn(authorities);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        Identity identity = Identity.builder().build();
        when(identityRepository.findById(eq(1L))).thenReturn(Optional.of(identity));

        boolean result1 = permissionIdentity.canWrite(1L, userDetails);
        assertFalse(result1);
    }

    @Test
    public void canWrite4() {
        Collection authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority(ROLE_ADMIN));
        when(authentication.getAuthorities()).thenReturn(authorities);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        Identity identity = Identity.builder().build();
        when(identityRepository.findById(eq(1L))).thenReturn(Optional.of(identity));

        boolean result1 = permissionIdentity.canWrite(1L, userDetails);
        assertTrue(result1);
    }

    @Test
    public void canWrite5() {
        Application application = Application.builder().id(2L).build();
        Identity identity = Identity.builder().availableApplication(application).build();
        when(identityRepository.findById(eq(1L))).thenReturn(Optional.of(identity));
        when(permissionApplication.canWrite(eq(2L), eq(userDetails))).thenReturn(true);

        boolean result1 = permissionIdentity.canWrite(1L, userDetails);
        assertTrue(result1);
        verify(permissionApplication, times(1)).canWrite(eq(2L), eq(userDetails));

    }
}
