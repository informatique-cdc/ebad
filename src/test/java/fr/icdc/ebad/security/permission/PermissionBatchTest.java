package fr.icdc.ebad.security.permission;

import fr.icdc.ebad.domain.User;
import fr.icdc.ebad.repository.UserRepository;
import fr.icdc.ebad.web.rest.dto.BatchDto;
import fr.icdc.ebad.web.rest.dto.BatchEnvironnementDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.HashSet;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PermissionBatchTest {
    @InjectMocks
    private PermissionBatch permissionBatch;

    @Mock
    private UserRepository userRepository;

    @Test
    public void canRead() {
        UserDetails userDetails = new org.springframework.security.core.userdetails.User("testLogin", "testPwd", new ArrayList<>());
        when(userRepository.findUserFromBatch(eq(1L), eq("testLogin"))).thenReturn(User.builder().login("testLogin").build());
        boolean result = permissionBatch.canRead(1L, userDetails);
        assertTrue(result);
    }

    @Test
    public void canWrite() {
        UserDetails userDetails = new org.springframework.security.core.userdetails.User("testLogin", "testPwd", new ArrayList<>());
        when(userRepository.findManagerFromBatch(eq(1L), eq("testLogin"))).thenReturn(User.builder().login("testLogin").build());
        boolean result = permissionBatch.canWrite(1L, userDetails);
        assertTrue(result);
    }

    @Test
    public void testCanRead() {
        UserDetails userDetails = new org.springframework.security.core.userdetails.User("testLogin", "testPwd", new ArrayList<>());
        when(userRepository.findUserFromBatch(eq(1L), eq("testLogin"))).thenReturn(User.builder().login("testLogin").build());
        BatchDto batch = new BatchDto();
        batch.setId(1L);
        boolean result = permissionBatch.canRead(batch, userDetails);
        assertTrue(result);
    }

    @Test
    public void testCanWrite() {
        UserDetails userDetails = new org.springframework.security.core.userdetails.User("testLogin", "testPwd", new ArrayList<>());
        when(userRepository.findManagerFromEnv(eq(2L), eq("testLogin"))).thenReturn(User.builder().login("testLogin").build());

        BatchEnvironnementDto batchEnvironnementDto = new BatchEnvironnementDto();
        batchEnvironnementDto.setId(2L);

        BatchDto batch = new BatchDto();
        HashSet hashSet = new HashSet();
        hashSet.add(batchEnvironnementDto);
        batch.setEnvironnements(hashSet);

        boolean result = permissionBatch.canWrite(batch, userDetails);
        assertTrue(result);
    }
}
