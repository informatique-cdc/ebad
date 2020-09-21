package fr.icdc.ebad.web.rest;

import com.querydsl.core.types.Predicate;
import fr.icdc.ebad.config.Constants;
import fr.icdc.ebad.domain.Application;
import fr.icdc.ebad.domain.Environnement;
import fr.icdc.ebad.security.permission.PermissionApplication;
import fr.icdc.ebad.service.EnvironnementService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.querydsl.QuerydslPredicateArgumentResolver;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ActiveProfiles(Constants.SPRING_PROFILE_TEST)
@SpringBootTest
public class EnvironnementResourceTest {
    @Autowired
    private EnvironnementResource environnementResource;

    @MockBean
    private EnvironnementService environnementService;

    @MockBean
    private PermissionApplication permissionApplication;

    @Autowired
    private QuerydslPredicateArgumentResolver querydslPredicateArgumentResolver;

    private MockMvc restMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.restMvc = MockMvcBuilders.standaloneSetup(environnementResource)
                .setCustomArgumentResolvers(querydslPredicateArgumentResolver, new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @WithMockUser
    public void getEnvironmentsFromApp() throws Exception {
        Application application = Application.builder().id(1L).build();
        Environnement environnement1 = Environnement.builder()
                .application(application)
                .name("myEnv1")
                .build();
        Environnement environnement2 = Environnement.builder()
                .application(application)
                .name("myEnv2")
                .build();
        List<Environnement> environnementList = new ArrayList<>();
        environnementList.add(environnement1);
        environnementList.add(environnement2);
        Page<Environnement> environnementPage = new PageImpl<>(environnementList);
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/environments?applicationId=1");
        when(environnementService.getEnvironmentFromApp(eq(1L), any(Predicate.class), any())).thenReturn(environnementPage);
        when(permissionApplication.canRead(eq(1L), any())).thenReturn(true);
        when(permissionApplication.canManage(eq(1L), any())).thenReturn(false);

        restMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].name", is("myEnv1")))
                .andExpect(jsonPath("$.content[1].name", is("myEnv2")));
    }

    @Test
    public void getInfo() {
    }

    @Test
    public void get() {
    }

    @Test
    public void purgeLog() {
    }

    @Test
    public void purgeArchive() {
    }

    @Test
    public void changeDateTraitement() {
    }

    @Test
    public void addEnvironnement() {
    }

    @Test
    public void updateEnvironnement() {
    }

    @Test
    public void deleteEnvironnement() {
    }

    @Test
    public void importEnvApp() {
    }

    @Test
    public void importAll() {
    }
}
