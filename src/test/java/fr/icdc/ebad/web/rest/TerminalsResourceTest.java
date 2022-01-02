package fr.icdc.ebad.web.rest;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.icdc.ebad.config.Constants;
import fr.icdc.ebad.security.permission.PermissionEnvironnement;
import fr.icdc.ebad.security.permission.PermissionServiceOpen;
import fr.icdc.ebad.service.EnvironnementService;
import fr.icdc.ebad.service.ShellService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.security.Principal;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ActiveProfiles(Constants.SPRING_PROFILE_TEST)
@SpringBootTest
public class TerminalsResourceTest {
    @MockBean
    private ShellService shellService;

    @MockBean
    private EnvironnementService environnementService;

    @MockBean
    private PermissionEnvironnement permissionEnvironnement;

    @MockBean
    private PermissionServiceOpen permissionServiceOpen;

    @Autowired
    private TerminalsResource terminalsResource;

    private MockMvc restMvc;
    private WebSocketStompClient webSocketStompClient;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setup() {
        System.out.println("setup");
        MockitoAnnotations.initMocks(this);
        restMvc = MockMvcBuilders
                .standaloneSetup(terminalsResource)
                .build();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Before
    public void setupWebSocket() {
        this.webSocketStompClient = new WebSocketStompClient(new SockJsClient(
                List.of(new WebSocketTransport(new StandardWebSocketClient()))));
    }


    @Test
    @WithMockUser(username = "user", roles = {"ADMIN"})
    public void startTerminal() throws Exception {
        Principal mockPrincipal = Mockito.mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("user");
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/terminals/1").principal(mockPrincipal);

        when(permissionServiceOpen.canRunTerminal()).thenReturn(true);
        when(permissionEnvironnement.canWrite(eq(1L),any())).thenReturn(true);

        restMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()));
    }
}
