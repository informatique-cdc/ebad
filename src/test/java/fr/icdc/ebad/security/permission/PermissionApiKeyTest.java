package fr.icdc.ebad.security.permission;

import fr.icdc.ebad.domain.ApiToken;
import fr.icdc.ebad.domain.User;
import fr.icdc.ebad.repository.ApiTokenRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PermissionApiKeyTest {
    @InjectMocks
    private PermissionApiKey permissionApiKey;

    @Mock
    private ApiTokenRepository apiTokenRepository;

    @Test
    public void canReadWrite1() {
        UserDetails userDetails = new org.springframework.security.core.userdetails.User("testLogin", "testPwd", new ArrayList<>());
        boolean result = permissionApiKey.canReadWrite(null, userDetails);
        assertFalse(result);
    }

    @Test
    public void canReadWrite2() {
        UserDetails userDetails = new org.springframework.security.core.userdetails.User("testLogin", "testPwd", new ArrayList<>());
        when(apiTokenRepository.findById(eq(1L))).thenReturn(Optional.empty());
        boolean result = permissionApiKey.canReadWrite(1L, userDetails);
        assertFalse(result);
    }

    @Test
    public void canReadWrite3() {
        UserDetails userDetails = new org.springframework.security.core.userdetails.User("testLogin", "testPwd", new ArrayList<>());
        User user = User.builder().id(1L).login("notSameLogin").build();
        ApiToken apiToken = ApiToken.builder().id(1L).token("encodedToken").user(user).name("newToken").build();

        when(apiTokenRepository.findById(eq(1L))).thenReturn(Optional.of(apiToken));

        boolean result = permissionApiKey.canReadWrite(1L, userDetails);
        assertFalse(result);
    }

    @Test
    public void canReadWrite4() {
        UserDetails userDetails = new org.springframework.security.core.userdetails.User("testLogin", "testPwd", new ArrayList<>());
        User user = User.builder().id(1L).login("testLogin").build();
        ApiToken apiToken = ApiToken.builder().id(1L).token("encodedToken").user(user).name("newToken").build();

        when(apiTokenRepository.findById(eq(1L))).thenReturn(Optional.of(apiToken));

        boolean result = permissionApiKey.canReadWrite(1L, userDetails);
        assertTrue(result);
    }
}
