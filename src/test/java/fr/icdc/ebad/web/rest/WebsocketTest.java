package fr.icdc.ebad.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.icdc.ebad.config.Constants;
import fr.icdc.ebad.domain.Authority;
import fr.icdc.ebad.domain.Environnement;
import fr.icdc.ebad.domain.Terminal;
import fr.icdc.ebad.domain.User;
import fr.icdc.ebad.repository.TerminalRepository;
import fr.icdc.ebad.repository.UserRepository;
import fr.icdc.ebad.security.jwt.TokenProvider;
import fr.icdc.ebad.security.permission.PermissionEnvironnement;
import fr.icdc.ebad.security.permission.PermissionServiceOpen;
import fr.icdc.ebad.service.EnvironnementService;
import fr.icdc.ebad.service.ShellService;
import fr.icdc.ebad.service.TerminalService;
import fr.icdc.ebad.service.UserService;
import fr.icdc.ebad.web.rest.dto.NewTerminalDto;
import fr.icdc.ebad.web.rest.dto.TerminalCommandDto;
import org.apache.sshd.client.channel.ChannelShell;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.session.Session;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import javax.security.auth.login.LoginContext;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles({Constants.SPRING_PROFILE_TEST, "jwt"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebsocketTest {
    @Value("${local.server.port}")
    private int port;
    private String URL;

    @MockBean
    private TokenProvider tokenProvider;

    @MockBean
    private PermissionEnvironnement permissionEnvironnement;
    @MockBean
    private UserService userService;

    @MockBean
    private TerminalService terminalService;

    @MockBean
    private ShellService shellService;

    @MockBean
    private EnvironnementService environnementService;

    @MockBean
    private PermissionServiceOpen permissionServiceOpen;
    private static final String SEND_TERMINAL_ENDPOINT = "/ebad/terminal";
    private static final String SUBSCRIBE_TERMINAL_ENDPOINT = "/user/queue/terminal-";
    private MockMvc restMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    private CompletableFuture<String> completableFuture;

    @Autowired
    private WebApplicationContext context;

    @Before
    public void setup() {
        completableFuture = new CompletableFuture<>();
        URL = "ws://localhost:" + port + "/ebad/ws";
        MockitoAnnotations.initMocks(this);
        restMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();
        objectMapper.registerModule(new JavaTimeModule());
    }


    @Test
    @WithMockUser
    public void testCreateGameEndpoint() throws Exception {
        String uuid = UUID.randomUUID().toString();

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("USER"));
        when(tokenProvider.getAuthentication(eq("toto"))).thenReturn(new UsernamePasswordAuthenticationToken("user", "toto", authorities));

        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        WebSocketHttpHeaders handshakeHeaders = new WebSocketHttpHeaders();
        StompHeaders connectHeaders = new StompHeaders();
        connectHeaders.add("Authorization", "toto");

        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        StompSession stompSession = stompClient.connect(URL,handshakeHeaders, connectHeaders, new StompSessionHandlerAdapter() {
        }).get(100, SECONDS);

        Principal mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("user");
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/terminals/1").principal(mockPrincipal);
        when(userService.getUser(any())).thenReturn(Optional.ofNullable(User.builder().id(1L).login("user").build()));
        when(terminalService.save(any())).thenReturn(Terminal.builder().id(UUID.fromString(uuid)).build());
        when(permissionServiceOpen.canRunTerminal()).thenReturn(true);
        when(permissionEnvironnement.canWrite(eq(1L),any())).thenReturn(true);
        Environnement env = Environnement.builder().build();
        when(environnementService.getEnvironnement(1L)).thenReturn(env);

        MvcResult mvcResult = restMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue())).andReturn();
        String json = mvcResult.getResponse().getContentAsString();
        NewTerminalDto newTerminalDto = new ObjectMapper().readValue(json, NewTerminalDto.class);

        ChannelShell channelShellMock = mock(ChannelShell.class);
        when(shellService.startShell(uuid)).thenReturn(channelShellMock);
        when(shellService.getLocalChannelShell(uuid)).thenReturn(channelShellMock);
        when(terminalService.findById(UUID.fromString(uuid))).thenReturn(Optional.of(Terminal.builder().user(User.builder().login("user").build()).id(UUID.fromString(uuid)).build()));
        OutputStream outputStream = mock(OutputStream.class);
        when(channelShellMock.getInvertedIn()).thenReturn(outputStream);
        when(channelShellMock.getSession()).thenReturn(mock(Session.class));
        when(channelShellMock.getClientSession()).thenReturn(mock(ClientSession.class));


        when(terminalService.findById(UUID.fromString(uuid))).thenReturn(Optional.of(Terminal.builder().id(UUID.fromString(uuid)).user(User.builder().login("user").build()).build()));
        StompSession.Subscription subscription = stompSession.subscribe(SUBSCRIBE_TERMINAL_ENDPOINT + newTerminalDto.getId(), new StringStompFrameHandler());
        TerminalCommandDto terminalCommandDto = new TerminalCommandDto();
        terminalCommandDto.setId(newTerminalDto.getId());
        terminalCommandDto.setKey("l");

        stompSession.send(SEND_TERMINAL_ENDPOINT, terminalCommandDto);
        Thread.sleep(2000);
        verify(permissionEnvironnement).canWrite(eq(1L),any());
        verify(environnementService).getEnvironnement(1L);
        verify(shellService).startShell(uuid);
        verify(channelShellMock).getInvertedIn();
        verify(outputStream).write("l".getBytes());

        subscription.unsubscribe();
        Thread.sleep(2000);

//        verify(channelShellMock).getSession();
//        verify(channelShellMock).getClientSession();
//        verify(channelShellMock).close(true);

    }


    private class StringStompFrameHandler implements StompFrameHandler {
        @Override
        public Type getPayloadType(StompHeaders stompHeaders) {
            System.out.println(stompHeaders.toString());
            return String.class;
        }

        @Override
        public void handleFrame(StompHeaders stompHeaders, Object o) {
            System.out.println((String) o);
            completableFuture.complete((String) o);
        }
    }
}
