package fr.icdc.ebad.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.icdc.ebad.config.Constants;
import fr.icdc.ebad.domain.TypeFichier;
import fr.icdc.ebad.security.permission.PermissionApplication;
import fr.icdc.ebad.security.permission.PermissionEnvironnement;
import fr.icdc.ebad.service.TypeFichierService;
import fr.icdc.ebad.web.rest.dto.ApplicationSimpleDto;
import fr.icdc.ebad.web.rest.dto.TypeFichierDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
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

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ActiveProfiles(Constants.SPRING_PROFILE_TEST)
@SpringBootTest
public class TypeFichierResourceTest {
    @Autowired
    private TypeFichierResource typeFichierResource;

    @MockBean
    private TypeFichierService typeFichierService;

    @MockBean
    private PermissionEnvironnement permissionEnvironnement;

    @MockBean
    private PermissionApplication permissionApplication;

    @Autowired
    private QuerydslPredicateArgumentResolver querydslPredicateArgumentResolver;

    private MockMvc restMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.restMvc = MockMvcBuilders
                .standaloneSetup(typeFichierResource)
                .setCustomArgumentResolvers(querydslPredicateArgumentResolver, new PageableHandlerMethodArgumentResolver())
                .build();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @WithMockUser(username = "user", roles = {"ADMIN"})
    public void getAllFromEnv() throws Exception {
        Principal mockPrincipal = Mockito.mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("user");
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/file-kinds/application/1").principal(mockPrincipal);

        TypeFichier typeFichier1 = new TypeFichier();
        typeFichier1.setId(1L);
        TypeFichier typeFichier2 = new TypeFichier();
        typeFichier2.setId(2L);

        List<TypeFichier> typeFichiers = new ArrayList<>();
        typeFichiers.add(typeFichier1);
        typeFichiers.add(typeFichier2);

        Page<TypeFichier> typeFichierPage = new PageImpl<>(typeFichiers);

        when(typeFichierService.getTypeFichierFromApplication(any(), any(), eq(1L))).thenReturn(typeFichierPage);
        when(permissionApplication.canRead(eq(1L), any())).thenReturn(true);
        restMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[1].id", is(2)));
    }

    @Test
    @WithMockUser(username = "user", roles = {"ADMIN"})
    public void addTypeFichier() throws Exception {
        Principal mockPrincipal = Mockito.mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("user");

        ApplicationSimpleDto applicationSimpleDto = new ApplicationSimpleDto();
        applicationSimpleDto.setId(1L);

        TypeFichierDto typeFichierDto = new TypeFichierDto();
        typeFichierDto.setApplication(applicationSimpleDto);
        typeFichierDto.setName("testName");
        typeFichierDto.setPattern("testPattern");

        TypeFichier typeFichier = new TypeFichier();
        typeFichier.setId(2L);
        typeFichier.setName("testName");
        typeFichier.setPattern("testPattern");

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.put("/file-kinds")
                .content(objectMapper.writeValueAsString(typeFichierDto))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .principal(mockPrincipal);


        when(typeFichierService.saveTypeFichier(argThat((thisTypeFichier) -> typeFichier.getName().equals(thisTypeFichier.getName())
                && typeFichier.getPattern().equals(thisTypeFichier.getPattern())))).thenReturn(typeFichier);

        when(permissionApplication.canWrite(eq(1L), any())).thenReturn(true);

        restMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.name", is(typeFichier.getName())))
                .andExpect(jsonPath("$.pattern", is(typeFichier.getPattern())));
    }

    @Test
    @WithMockUser(username = "user", roles = {"ADMIN"})
    public void removeTypeFichier() throws Exception {
        Principal mockPrincipal = Mockito.mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("user");

        ApplicationSimpleDto applicationSimpleDto = new ApplicationSimpleDto();
        applicationSimpleDto.setId(1L);

        TypeFichierDto typeFichierDto = new TypeFichierDto();
        typeFichierDto.setApplication(applicationSimpleDto);
        typeFichierDto.setId(2L);

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/file-kinds/delete")
                .content(objectMapper.writeValueAsString(typeFichierDto))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .principal(mockPrincipal);

        when(permissionApplication.canWrite(eq(1L), any())).thenReturn(true);

        restMvc.perform(builder).andExpect(status().isOk());
        verify(typeFichierService).deleteTypeFichier(eq(2L));
    }

    @Test
    @WithMockUser(username = "user", roles = {"ADMIN"})
    public void updateTypeFichier() throws Exception {
        Principal mockPrincipal = Mockito.mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("user");

        ApplicationSimpleDto applicationSimpleDto = new ApplicationSimpleDto();
        applicationSimpleDto.setId(1L);

        TypeFichierDto typeFichierDto = new TypeFichierDto();
        typeFichierDto.setApplication(applicationSimpleDto);
        typeFichierDto.setName("testName");
        typeFichierDto.setId(2L);
        typeFichierDto.setPattern("testPattern");

        TypeFichier typeFichier = new TypeFichier();
        typeFichier.setId(2L);
        typeFichier.setName("testName");
        typeFichier.setPattern("testPattern");

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.patch("/file-kinds")
                .content(objectMapper.writeValueAsString(typeFichierDto))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .principal(mockPrincipal);


        when(typeFichierService.saveTypeFichier(argThat((thisTypeFichier) -> typeFichier.getName().equals(thisTypeFichier.getName())
                && typeFichier.getPattern().equals(thisTypeFichier.getPattern())))).thenReturn(typeFichier);

        when(permissionApplication.canWrite(eq(1L), any())).thenReturn(true);

        restMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.name", is(typeFichier.getName())))
                .andExpect(jsonPath("$.pattern", is(typeFichier.getPattern())));
    }
}
