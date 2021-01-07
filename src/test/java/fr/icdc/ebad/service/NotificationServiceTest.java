package fr.icdc.ebad.service;

import fr.icdc.ebad.config.Constants;
import fr.icdc.ebad.domain.Notification;
import fr.icdc.ebad.domain.User;
import fr.icdc.ebad.repository.NotificationRepository;
import fr.icdc.ebad.repository.UserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by DTROUILLET on 22/03/2018.
 */
@RunWith(SpringRunner.class)
@ActiveProfiles(Constants.SPRING_PROFILE_TEST)
@SpringBootTest
public class NotificationServiceTest {

    @MockBean
    private NotificationRepository notificationRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private NotificationService notificationService;

    @Test
    @WithMockUser(username = "user")
    public void createNotificationForCurrentUser() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setLogin("user");
        when(userRepository.findOneByLoginUser(eq("user"))).thenReturn(Optional.of(user));
        when(notificationRepository.save(any())).thenReturn(null);

        notificationService.createNotificationForCurrentUser("ceci est un test");

        ArgumentCaptor<Notification> argument = ArgumentCaptor.forClass(Notification.class);

        verify(notificationRepository,times(1)).save(argument.capture());

        assertEquals("ceci est un test", argument.getValue().getContent());
        assertEquals(user, argument.getValue().getReceiver());
        assertFalse(argument.getValue().isRead());
    }

}
