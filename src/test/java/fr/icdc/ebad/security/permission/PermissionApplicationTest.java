package fr.icdc.ebad.security.permission;

import fr.icdc.ebad.config.Constants;
import fr.icdc.ebad.domain.User;
import fr.icdc.ebad.repository.UserRepository;
import fr.icdc.ebad.web.rest.dto.ApplicationDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PermissionApplicationTest {

    @InjectMocks
    private PermissionApplication permissionApplication;

    @Mock
    private UserRepository userRepository;

    @Test
    public void canRead() {
        UserDetails userDetails = new org.springframework.security.core.userdetails.User("testLogin", "testPwd", new ArrayList<>());
        when(userRepository.findUserFromApplication(eq(1L), eq("testLogin"))).thenReturn(User.builder().login("testLogin").build());
        boolean result = permissionApplication.canRead(1L, userDetails);
        assertTrue(result);
    }

    @Test
    public void canManage() {
        ApplicationDto applicationDto = new ApplicationDto();
        applicationDto.setId(1L);
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(new SimpleGrantedAuthority(Constants.ROLE_ADMIN));
        UserDetails userDetails = new org.springframework.security.core.userdetails.User("testLogin", "testPwd", grantedAuthorities);
        boolean result = permissionApplication.canManage(applicationDto, userDetails);
        assertTrue(result);
    }

    @Test
    public void canWrite() {
        UserDetails userDetails = new org.springframework.security.core.userdetails.User("testLogin", "testPwd", new ArrayList<>());
        when(userRepository.findManagerFromApplication(eq(2L), eq("testLogin"))).thenReturn(User.builder().login("testLogin").build());
        boolean result = permissionApplication.canWrite(2L, userDetails);
        assertTrue(result);
    }

    @Test
    public void testCanRead() {
        UserDetails userDetails = new org.springframework.security.core.userdetails.User("testLogin", "testPwd", new ArrayList<>());
        ApplicationDto applicationDto = new ApplicationDto();
        applicationDto.setId(1L);
        when(userRepository.findUserFromApplication(eq(1L), eq("testLogin"))).thenReturn(User.builder().login("testLogin").build());
        boolean result = permissionApplication.canRead(applicationDto, userDetails);
        assertTrue(result);
    }

    @Test
    public void testCanWrite() {
        UserDetails userDetails = new org.springframework.security.core.userdetails.User("testLogin", "testPwd", new ArrayList<>());
        ApplicationDto applicationDto = new ApplicationDto();
        applicationDto.setId(2L);
        when(userRepository.findManagerFromApplication(eq(2L), eq("testLogin"))).thenReturn(User.builder().login("testLogin").build());
        boolean result = permissionApplication.canWrite(applicationDto, userDetails);
        assertTrue(result);
    }

    @Test
    public void testCanManage() {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(new SimpleGrantedAuthority(Constants.ROLE_ADMIN));
        UserDetails userDetails = new org.springframework.security.core.userdetails.User("testLogin", "testPwd", grantedAuthorities);
        boolean result = permissionApplication.canManage(2L, userDetails);
        assertTrue(result);
    }
}
