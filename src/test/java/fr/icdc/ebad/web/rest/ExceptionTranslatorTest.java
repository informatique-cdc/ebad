package fr.icdc.ebad.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.icdc.ebad.config.Constants;
import fr.icdc.ebad.security.permission.PermissionEnvironnement;
import fr.icdc.ebad.service.ApplicationService;
import fr.icdc.ebad.service.NormeService;
import fr.icdc.ebad.service.util.EbadNotFoundException;
import fr.icdc.ebad.service.util.EbadServiceException;
import fr.icdc.ebad.util.TestUtil;
import fr.icdc.ebad.web.rest.dto.ApplicationDto;
import fr.icdc.ebad.web.rest.errors.ExceptionTranslator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.management.RuntimeMBeanException;
import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolationException;
import java.util.Locale;
import java.util.Set;

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
    NormResource normResource;

    @Autowired
    ApplicationResource applicatiResource;

    @Autowired
    ExceptionTranslator exceptionTranslator;

    @MockBean
    PermissionEnvironnement permissionEnvironnement;

    @MockBean
    NormeService normeService;

    @MockBean
    ApplicationService applicationService;

    private MockMvc restMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setup() {
        Locale.setDefault( Locale.FRANCE );
        MockitoAnnotations.initMocks(this);
        this.restMvc = MockMvcBuilders.standaloneSetup(batchResource, normResource, applicatiResource).setControllerAdvice(exceptionTranslator).build();
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

    @Test
    @WithMockUser
    public void testHandleMissingServletRequestParameter() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/batchs/run/1");
        when(permissionEnvironnement.canRead(eq(2L), any())).thenReturn(true);

        restMvc.perform(builder)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.apierror.message", is("env parameter is missing")));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testHandleHttpMediaTypeNotSupported() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.put("/norms").contentType("application/notjson");

        restMvc.perform(builder)
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.apierror.message", is("application/notjsonMedia type is not supported. Supported media types are application/json, application/*+json")));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testHandleMethodArgumentNotValid() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.put("/norms").content("{}").contentType("application/json");

        restMvc.perform(builder)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.apierror.message", is("Error occured when validate field")));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testHandleConstraintViolation() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.put("/norms").content("{\"name\": \"toto\"}").contentType("application/json");
        ConstraintViolationException constraintViolationException = new ConstraintViolationException(Set.of());
        when(normeService.saveNorme(any())).thenThrow(constraintViolationException);
        restMvc.perform(builder)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.apierror.message", is("Error occured when validate field")));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testHandleHttpMessageNotReadable() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.put("/norms").content("\"name\": \"toto\"}").contentType("application/json");
        restMvc.perform(builder)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.apierror.message", is("Malformed JSON request")));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testHandleEntityNotFound() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.put("/norms").content("{\"name\": \"toto\"}").contentType("application/json");
        when(normeService.saveNorme(any())).thenThrow(EntityNotFoundException.class);
        restMvc.perform(builder)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.apierror.message", is("Unexpected error")));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testHandleEbadNotFound() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.put("/norms").content("{\"name\": \"toto\"}").contentType("application/json");
        EbadNotFoundException ex = new EbadNotFoundException("not found test");
        when(normeService.saveNorme(any())).thenThrow(ex);
        restMvc.perform(builder)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.apierror.message", is("not found test")));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testHandleEbadServiceException() throws Exception {
        ApplicationDto applicationDto = new ApplicationDto();
        applicationDto.setId(1L);
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.patch("/applications/gestion").content(TestUtil.convertObjectToJsonBytes(applicationDto)).contentType("application/json");
        EbadServiceException ex = new EbadServiceException("error test");
        when(applicationService.updateApplication(any())).thenThrow(ex);
        restMvc.perform(builder)
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.apierror.message", is("error test")));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testHandleIllegalStateException() throws Exception {
        ApplicationDto applicationDto = new ApplicationDto();
        applicationDto.setId(1L);
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.patch("/applications/gestion").content(TestUtil.convertObjectToJsonBytes(applicationDto)).contentType("application/json");
        IllegalStateException ex = new IllegalStateException("error test");
        when(applicationService.updateApplication(any())).thenThrow(ex);
        restMvc.perform(builder)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.apierror.message", is("error test")));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testHandleDataIntegrityViolation() throws Exception {
        ApplicationDto applicationDto = new ApplicationDto();
        applicationDto.setId(1L);
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.patch("/applications/gestion").content(TestUtil.convertObjectToJsonBytes(applicationDto)).contentType("application/json");
        when(applicationService.updateApplication(any())).thenThrow(DataIntegrityViolationException.class);
        restMvc.perform(builder)
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.apierror.message", is("Unexpected error")));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testHandleDataIntegrityViolation2() throws Exception {
        ApplicationDto applicationDto = new ApplicationDto();
        applicationDto.setId(1L);
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.patch("/applications/gestion").content(TestUtil.convertObjectToJsonBytes(applicationDto)).contentType("application/json");
        DataIntegrityViolationException ex = new DataIntegrityViolationException("error test", new ConstraintViolationException(Set.of()));
        when(applicationService.updateApplication(any())).thenThrow(ex);
        restMvc.perform(builder)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.apierror.message", is("Database error")));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testHandleMethodArgumentTypeMismatch() throws Exception {
        ApplicationDto applicationDto = new ApplicationDto();
        applicationDto.setId(1L);
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.patch("/applications/gestion").content(TestUtil.convertObjectToJsonBytes(applicationDto)).contentType("application/json");
        MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException("test", Integer.class, "myInt", null, null);
        when(applicationService.updateApplication(any())).thenThrow(ex);
        restMvc.perform(builder)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.apierror.message", is("The parameter 'myInt' of value 'test' could not be converted to type 'Integer'")));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testHandleException() throws Exception {
        ApplicationDto applicationDto = new ApplicationDto();
        applicationDto.setId(1L);
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.patch("/applications/gestion").content(TestUtil.convertObjectToJsonBytes(applicationDto)).contentType("application/json");
        when(applicationService.updateApplication(any())).thenThrow(RuntimeMBeanException.class);
        restMvc.perform(builder)
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.apierror.message", is("Internal Server Error")));
    }
}
