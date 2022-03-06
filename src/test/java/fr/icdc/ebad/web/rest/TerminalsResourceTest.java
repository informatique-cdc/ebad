package fr.icdc.ebad.web.rest;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.icdc.ebad.config.Constants;
import fr.icdc.ebad.domain.Environnement;
import fr.icdc.ebad.domain.Terminal;
import fr.icdc.ebad.domain.User;
import fr.icdc.ebad.security.permission.PermissionEnvironnement;
import fr.icdc.ebad.security.permission.PermissionServiceOpen;
import fr.icdc.ebad.service.EnvironnementService;
import fr.icdc.ebad.service.TerminalService;
import fr.icdc.ebad.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
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
    private UserService userService;

    @MockBean
    private EnvironnementService environnementService;

    @MockBean
    private PermissionEnvironnement permissionEnvironnement;

    @MockBean
    private PermissionServiceOpen permissionServiceOpen;

    @MockBean
    private TerminalService terminalService;

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

        UUID uuid = UUID.randomUUID();
        Environnement environnement = Environnement.builder().id(2L).build();
        User user = User.builder().login("user").build();

        when(permissionServiceOpen.canRunTerminal()).thenReturn(true);
        when(permissionEnvironnement.canWrite(eq(1L),any())).thenReturn(true);

        when(environnementService.getEnvironnement(1L)).thenReturn(environnement);
        when(userService.getUser("user")).thenReturn(Optional.of(user));
        when(terminalService.save(any())).thenReturn(Terminal.builder().id(uuid).environment(environnement).user(user).build());
        restMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(uuid.toString())));
    }
}
