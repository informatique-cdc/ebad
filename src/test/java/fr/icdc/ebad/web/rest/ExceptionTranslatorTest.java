package fr.icdc.ebad.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.icdc.ebad.config.Constants;
import fr.icdc.ebad.security.permission.PermissionEnvironnement;
import fr.icdc.ebad.web.rest.errors.ExceptionTranslator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Locale;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ActiveProfiles(Constants.SPRING_PROFILE_TEST)
@SpringBootTest
public class ExceptionTranslatorTest {
    @Autowired
    BatchResource batchResource;

    @Autowired
    ExceptionTranslator exceptionTranslator;

    @MockBean
    PermissionEnvironnement permissionEnvironnement;

    private MockMvc restMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setup() {
        Locale.setDefault( Locale.FRANCE );
        MockitoAnnotations.initMocks(this);
        this.restMvc = MockMvcBuilders.standaloneSetup(batchResource).setControllerAdvice(exceptionTranslator).build();
    }

    @Test
    @WithMockUser
    public void testPermissionDenied() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/batchs/run/1?env=2");
        when(permissionEnvironnement.canRead(eq(2L), any())).thenReturn(false);

        restMvc.perform(builder)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.apierror.message", is("Vous ne disposez pas d'authorisations suffisantes pour accéder à cette ressource")));
    }

    @Test
    @WithMockUser
    public void testInsufficientAuthenticationException() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/batchs/run/1?env=2");
        when(permissionEnvironnement.canRead(eq(2L), any())).thenThrow(InsufficientAuthenticationException.class);

        restMvc.perform(builder)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.apierror.message", is("Vous ne disposez pas d'authorisations suffisantes pour accéder à cette ressource")));
    }
}
