package fr.icdc.ebad.security.permission;

import fr.icdc.ebad.domain.User;
import fr.icdc.ebad.repository.UserRepository;
import fr.icdc.ebad.web.rest.dto.EnvironnementDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PermissionEnvironnementTest {

    @Mock
    UserDetails userDetails;
    @InjectMocks
    private PermissionEnvironnement permissionEnvironnement;
    @Mock
    private UserRepository userRepository;

    @Test
    public void canReadLong() {
        when(userDetails.getUsername()).thenReturn("testlogin");
        when(userRepository.findUserFromEnv(eq(1L), eq("testlogin"))).thenReturn(User.builder().build());
        boolean result = permissionEnvironnement.canRead(1L, userDetails);
        assertTrue(result);
    }

    @Test
    public void cantReadLong() {
        when(userDetails.getUsername()).thenReturn("testlogin");
        when(userRepository.findUserFromEnv(eq(1L), eq("testlogin"))).thenReturn(null);
        boolean result = permissionEnvironnement.canRead(1L, userDetails);
        assertFalse(result);
    }

    @Test
    public void canRead() {
        EnvironnementDto environnementDto = EnvironnementDto.builder().id(1L).build();
        when(userDetails.getUsername()).thenReturn("testlogin");
        when(userRepository.findUserFromEnv(eq(1L), eq("testlogin"))).thenReturn(User.builder().build());
        boolean result = permissionEnvironnement.canRead(environnementDto, userDetails);
        assertTrue(result);
    }

    @Test
    public void cantRead() {
        EnvironnementDto environnementDto = EnvironnementDto.builder().id(1L).build();
        when(userDetails.getUsername()).thenReturn("testlogin");
        when(userRepository.findUserFromEnv(eq(1L), eq("testlogin"))).thenReturn(null);
        boolean result = permissionEnvironnement.canRead(environnementDto, userDetails);
        assertFalse(result);
    }

    @Test
    public void cantReadNull() {
        EnvironnementDto environnementDto = EnvironnementDto.builder().build();
        boolean result = permissionEnvironnement.canRead(environnementDto, userDetails);
        assertFalse(result);

        boolean result2 = permissionEnvironnement.canRead((EnvironnementDto) null, userDetails);
        assertFalse(result2);
    }

    @Test
    public void canWriteLong() {
        when(userDetails.getUsername()).thenReturn("testlogin");
        when(userRepository.findManagerFromEnv(eq(1L), eq("testlogin"))).thenReturn(User.builder().build());
        boolean result = permissionEnvironnement.canWrite(1L, userDetails);
        assertTrue(result);
    }

    @Test
    public void cantWriteLong() {
        when(userDetails.getUsername()).thenReturn("testlogin");
        when(userRepository.findManagerFromEnv(eq(1L), eq("testlogin"))).thenReturn(null);
        boolean result = permissionEnvironnement.canWrite(1L, userDetails);
        assertFalse(result);
    }

    @Test
    public void canWrite() {
        EnvironnementDto environnementDto = EnvironnementDto.builder().id(1L).build();
        when(userDetails.getUsername()).thenReturn("testlogin");
        when(userRepository.findManagerFromEnv(eq(1L), eq("testlogin"))).thenReturn(User.builder().build());
        boolean result = permissionEnvironnement.canWrite(environnementDto, userDetails);
        assertTrue(result);
    }

    @Test
    public void cantWrite() {
        EnvironnementDto environnementDto = EnvironnementDto.builder().id(1L).build();
        when(userDetails.getUsername()).thenReturn("testlogin");
        when(userRepository.findManagerFromEnv(eq(1L), eq("testlogin"))).thenReturn(null);
        boolean result = permissionEnvironnement.canWrite(environnementDto, userDetails);
        assertFalse(result);
    }

    @Test
    public void cantWriteNull() {
        EnvironnementDto environnementDto = EnvironnementDto.builder().build();
        boolean result = permissionEnvironnement.canWrite(environnementDto, userDetails);
        assertFalse(result);

        boolean result2 = permissionEnvironnement.canWrite((EnvironnementDto) null, userDetails);
        assertFalse(result2);
    }
}
