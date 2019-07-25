package fr.icdc.ebad.web.rest;

import fr.icdc.ebad.config.Constants;
import fr.icdc.ebad.domain.Notification;
import fr.icdc.ebad.repository.NotificationRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ActiveProfiles(Constants.SPRING_PROFILE_TEST)
@SpringBootTest
public class NotificationResourceTest {
    @MockBean
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationResource notificationResource;

    private MockMvc restMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.restMvc = MockMvcBuilders.standaloneSetup(notificationResource).build();
    }

    @Test
    @WithMockUser(username = "user")
    public void getUnreadNotification() throws Exception {
        Notification notification1 = new Notification();
        notification1.setRead(false);
        notification1.setContent("ceci est un test 1");
        notification1.setId(1L);

        Notification notification2 = new Notification();
        notification2.setRead(false);
        notification2.setContent("ceci est un test 2");
        notification2.setId(2L);

        List<Notification> notificationList = new ArrayList<>();
        notificationList.add(notification1);
        notificationList.add(notification2);

        when(notificationRepository.findByReceiverLoginAndReadFalse(any(Sort.class),eq("user"))).thenReturn(notificationList);

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/api/notifications");
        restMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$",hasSize(2)))
                .andExpect(jsonPath("$[0].id",is(1)))
                .andExpect(jsonPath("$[0].content",is("ceci est un test 1")))
                .andExpect(jsonPath("$[1].content",is("ceci est un test 2")))
                .andExpect(jsonPath("$[1].id",is(2)));
    }

    @Test
    @WithMockUser(username = "user")
    public void markAsReadNotification() throws Exception {
        Notification notification1 = new Notification();
        notification1.setRead(false);
        notification1.setContent("ceci est un test 1");
        notification1.setId(1L);

        Notification notification2 = new Notification();
        notification2.setRead(false);
        notification2.setContent("ceci est un test 2");
        notification2.setId(2L);

        List<Notification> notificationList = new ArrayList<>();
        notificationList.add(notification1);
        notificationList.add(notification2);

        when(notificationRepository.findByReceiverLoginAndReadFalse(any(Sort.class),eq("user"))).thenReturn(notificationList);
        when(notificationRepository.save(any())).thenReturn(null);

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.patch("/api/notifications");
        restMvc.perform(builder)
                .andExpect(status().isOk());

        ArgumentCaptor<Notification> argument = ArgumentCaptor.forClass(Notification.class);

        verify(notificationRepository,times(2)).save(argument.capture());

        assertEquals(notification1,argument.getAllValues().get(0));
        assertTrue(argument.getAllValues().get(0).isRead());
        assertEquals(notification2,argument.getAllValues().get(1));
        assertTrue(argument.getAllValues().get(1).isRead());

    }

}
