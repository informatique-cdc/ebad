package fr.icdc.ebad.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.icdc.ebad.config.Constants;
import fr.icdc.ebad.domain.Application;
import fr.icdc.ebad.domain.User;
import fr.icdc.ebad.repository.AuthorityRepository;
import fr.icdc.ebad.repository.UserRepository;
import fr.icdc.ebad.security.permission.PermissionServiceOpen;
import fr.icdc.ebad.service.ApplicationService;
import fr.icdc.ebad.web.rest.dto.ApplicationDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.querydsl.QuerydslPredicateArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ActiveProfiles(Constants.SPRING_PROFILE_TEST)
@SpringBootTest
public class ApplicationResourceTest {
    @Autowired
    private ApplicationResource applicationResource;

    @MockBean
    private ApplicationService applicationService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private AuthorityRepository authorityRepository;

    @MockBean
    private PermissionServiceOpen permissionServiceOpen;

    @Autowired
    private QuerydslPredicateArgumentResolver querydslPredicateArgumentResolver;

    private MockMvc restMvc;

    @Autowired
    private WebApplicationContext context;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.restMvc = MockMvcBuilders
                .standaloneSetup(applicationResource)
                .setCustomArgumentResolvers(querydslPredicateArgumentResolver, new PageableHandlerMethodArgumentResolver())
                .build();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @WithMockUser(username = "user", roles = {"ADMIN"})
    public void findApplication() throws Exception {
        Principal mockPrincipal = Mockito.mock(Principal.class);
        Mockito.when(mockPrincipal.getName()).thenReturn("user");
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/applications/search?name=test").principal(mockPrincipal);

        List<Application> applications = new ArrayList<>();
        Application application1 = new Application();
        application1.setId(1L);
        applications.add(application1);

        Application application2 = new Application();
        application2.setId(2L);
        applications.add(application2);
        PageImpl<Application> applicationPage = new PageImpl<>(applications);

        when(applicationService.findApplication(any(), any())).thenReturn(applicationPage);

        restMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[1].id", is(2)));
    }


    @Test
    @WithMockUser(username = "user", roles = {"ADMIN"})
    public void getAll() throws Exception {
        Principal mockPrincipal = Mockito.mock(Principal.class);
        Mockito.when(mockPrincipal.getName()).thenReturn("user");
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/applications").principal(mockPrincipal);

        List<Application> applications = new ArrayList<>();
        Application application1 = new Application();
        application1.setId(1L);
        applications.add(application1);

        Application application2 = new Application();
        application2.setId(2L);
        applications.add(application2);
        PageImpl<Application> applicationPage = new PageImpl<>(applications);

        when(applicationService.getAllApplicationsUsed(any(), eq("user"))).thenReturn(applicationPage);
        when(userRepository.findUserFromApplication(anyLong(), anyString())).thenReturn(new User());

        restMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[1].id", is(2)));
    }

    @Test
    @WithMockUser(username = "dtrouillet")
    public void getAllWrite() throws Exception {
        Principal mockPrincipal = Mockito.mock(Principal.class);
        Mockito.when(mockPrincipal.getName()).thenReturn("dtrouillet");
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/applications/write").principal(mockPrincipal);

        List<Application> applications = new ArrayList<>();
        Application application1 = new Application();
        application1.setId(1L);
        applications.add(application1);

        Application application2 = new Application();
        application2.setId(2L);
        applications.add(application2);

        PageImpl<Application> applicationPage = new PageImpl<>(applications);

        when(applicationService.getAllApplicationsManaged(any(), eq("dtrouillet"))).thenReturn(applicationPage);
        when(userRepository.findManagerFromApplication(eq(1L), eq("dtrouillet"))).thenReturn(new User());

        restMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id", is(1)));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void getAllManage() throws Exception {
        Principal mockPrincipal = Mockito.mock(Principal.class);
        Mockito.when(mockPrincipal.getName()).thenReturn("user");
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/applications/gestion").principal(mockPrincipal);

        List<Application> applications = new ArrayList<>();
        Application application1 = new Application();
        application1.setId(1L);
        applications.add(application1);

        Application application2 = new Application();
        application2.setId(2L);
        applications.add(application2);

        PageImpl<Application> applicationPage = new PageImpl<>(applications);

        when(applicationService.getAllApplications(any(), any())).thenReturn(applicationPage);
        restMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[1].id", is(2)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void createApplication() throws Exception {
        ApplicationDto applicationDto = new ApplicationDto();
        applicationDto.setCode("AA0");
        applicationDto.setDateFichierPattern("yyyyMMdd");
        applicationDto.setDateParametrePattern("ddMMyyyy");
        applicationDto.setName("MyApp");

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .put("/applications/gestion")
                .content(objectMapper.writeValueAsString(applicationDto))
                .contentType(MediaType.APPLICATION_JSON_UTF8);


        Application application = Application.builder()
                .id(1L)
                .code("AA0")
                .dateFichierPattern("yyyyMMdd")
                .dateParametrePattern("ddMMyyyy")
                .name("MyApp")
                .build();

        when(permissionServiceOpen.canCreateApplication()).thenReturn(true);
        when(applicationService.saveApplication(argThat((argApp ->
                argApp.getCode().equals("AA0") &&
                        argApp.getDateFichierPattern().equals("yyyyMMdd") &&
                        argApp.getDateParametrePattern().equals("ddMMyyyy") &&
                        argApp.getName().equals("MyApp")
        )))).thenReturn(application);

        restMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code", is("AA0")))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.dateFichierPattern", is("yyyyMMdd")))
                .andExpect(jsonPath("$.dateParametrePattern", is("ddMMyyyy")))
                .andExpect(jsonPath("$.name", is("MyApp")));
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    public void getUsersFromApplication() throws Exception {
        Set<User> users = new HashSet<>();
        User user1 = User.builder().id(1L).login("test1").build();
        users.add(user1);
        User user2 = User.builder().id(2L).login("test2").build();
        users.add(user2);
        when(applicationService.getUsers(eq(1L))).thenReturn(users);

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .get("/applications/users/1");


        restMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[0].login", is("test1")))
                .andExpect(jsonPath("$[1].login", is("test2")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void getModeratorsFromApplication() throws Exception {
        Set<User> users = new HashSet<>();
        User user1 = User.builder().id(1L).login("test1").build();
        users.add(user1);
        User user2 = User.builder().id(2L).login("test2").build();
        users.add(user2);
        when(applicationService.getManagers(eq(1L))).thenReturn(users);

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .get("/applications/moderators/1");


        restMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[0].login", is("test1")))
                .andExpect(jsonPath("$[1].login", is("test2")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void importAll() throws Exception {
        when(applicationService.importApp()).thenReturn("OK");
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/applications/import-all");
        restMvc.perform(builder).andExpect(status().isOk());
        verify(applicationService).importApp();
    }
}
