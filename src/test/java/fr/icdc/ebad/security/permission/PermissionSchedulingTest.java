package fr.icdc.ebad.security.permission;

import fr.icdc.ebad.domain.Batch;
import fr.icdc.ebad.domain.Environnement;
import fr.icdc.ebad.domain.Scheduling;
import fr.icdc.ebad.domain.User;
import fr.icdc.ebad.repository.SchedulingRepository;
import fr.icdc.ebad.repository.UserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PermissionSchedulingTest {
    @Mock
    UserDetails userDetails;

    @InjectMocks
    private PermissionScheduling permissionScheduling;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SchedulingRepository schedulingRepository;


    @Test
    public void canRead() {
        when(userDetails.getUsername()).thenReturn("testlogin");
        when(userRepository.findUserFromEnv(eq(2L), eq("testlogin"))).thenReturn(User.builder().build());

        Batch batch = Batch.builder().id(1L).build();
        Environnement environnement = Environnement.builder().id(2L).build();


        Scheduling scheduling = Scheduling.builder()
                .id(3L)
                .batch(batch)
                .environnement(environnement)
                .parameters("parameters")
                .cron("10 * * * * ?")
                .build();

        when(schedulingRepository.findById(3L)).thenReturn(Optional.of(scheduling));
        boolean result = permissionScheduling.canRead(3L, userDetails);
        assertTrue(result);
    }

    @Test
    public void canReadFalse1() {
        when(userDetails.getUsername()).thenReturn("testlogin");
        when(schedulingRepository.findById(3L)).thenReturn(Optional.empty());
        boolean result = permissionScheduling.canRead(3L, userDetails);
        assertFalse(result);
    }

    @Test
    public void canReadFalse2() {
        when(userDetails.getUsername()).thenReturn("testlogin");
        when(userRepository.findUserFromEnv(eq(2L), eq("testlogin"))).thenReturn(null);

        Batch batch = Batch.builder().id(1L).build();
        Environnement environnement = Environnement.builder().id(2L).build();


        Scheduling scheduling = Scheduling.builder()
                .id(3L)
                .batch(batch)
                .environnement(environnement)
                .parameters("parameters")
                .cron("10 * * * * ?")
                .build();

        when(schedulingRepository.findById(3L)).thenReturn(Optional.of(scheduling));
        boolean result = permissionScheduling.canRead(3L, userDetails);
        assertFalse(result);
    }
}
