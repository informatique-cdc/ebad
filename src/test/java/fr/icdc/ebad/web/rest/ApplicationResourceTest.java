package fr.icdc.ebad.web.rest;

import fr.icdc.ebad.config.Constants;
import fr.icdc.ebad.domain.Application;
import fr.icdc.ebad.domain.User;
import fr.icdc.ebad.repository.AuthorityRepository;
import fr.icdc.ebad.repository.UserRepository;
import fr.icdc.ebad.service.ApplicationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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

    private MockMvc restMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.restMvc = MockMvcBuilders.standaloneSetup(applicationResource).build();
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void getAll() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/api/application");

        List<Application> applications = new ArrayList<>();
        Application application1 = new Application();
        application1.setId(1L);
        applications.add(application1);

        Application application2 = new Application();
        application2.setId(2L);
        applications.add(application2);

        when(applicationService.getAllApplications()).thenReturn(applications);
        when(userRepository.findUserFromApplication(anyLong(), anyString())).thenReturn(new User());

        restMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));
    }

    @Test
    @WithMockUser(roles = {"MODO"}, username = "dtrouillet")
    public void getAllWrite() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/api/application/write");

        List<Application> applications = new ArrayList<>();
        Application application1 = new Application();
        application1.setId(1L);
        applications.add(application1);

        Application application2 = new Application();
        application2.setId(2L);
        applications.add(application2);

        when(applicationService.getAllApplications()).thenReturn(applications);
        when(userRepository.findManagerFromApplication(eq(1L), eq("dtrouillet"))).thenReturn(new User());

        restMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void getAllManage() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/api/application/gestion");

        List<Application> applications = new ArrayList<>();
        Application application1 = new Application();
        application1.setId(1L);
        applications.add(application1);

        Application application2 = new Application();
        application2.setId(2L);
        applications.add(application2);

        when(applicationService.getAllApplications()).thenReturn(applications);

        restMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    public void getAllManage2() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/api/application/gestion");

        List<Application> applications = new ArrayList<>();
        Application application1 = new Application();
        application1.setId(1L);
        applications.add(application1);

        Application application2 = new Application();
        application2.setId(2L);
        applications.add(application2);

        when(applicationService.getAllApplications()).thenReturn(applications);

        restMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void createApplication() {
    }

    @Test
    public void updateApplication() {
    }

    @Test
    public void removeApplication() {
    }

    @Test
    public void getUsersFromApplication() {
    }

    @Test
    public void getModeratorsFromApplication() {
    }
}
