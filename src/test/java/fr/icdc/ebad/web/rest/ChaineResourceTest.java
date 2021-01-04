package fr.icdc.ebad.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.icdc.ebad.config.Constants;
import fr.icdc.ebad.domain.Chaine;
import fr.icdc.ebad.domain.util.RetourBatch;
import fr.icdc.ebad.security.permission.PermissionChaine;
import fr.icdc.ebad.security.permission.PermissionEnvironnement;
import fr.icdc.ebad.service.ChaineService;
import fr.icdc.ebad.web.rest.dto.BatchEnvironnementDto;
import fr.icdc.ebad.web.rest.dto.ChaineDto;
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

import java.io.IOException;
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
public class ChaineResourceTest {
    @Autowired
    private ChaineResource chaineResource;

    @MockBean
    private ChaineService chaineService;

    @MockBean
    private PermissionEnvironnement permissionEnvironnement;

    @MockBean
    private PermissionChaine permissionChaine;

    @Autowired
    private QuerydslPredicateArgumentResolver querydslPredicateArgumentResolver;

    private MockMvc restMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.restMvc = MockMvcBuilders
                .standaloneSetup(chaineResource)
                .setCustomArgumentResolvers(querydslPredicateArgumentResolver, new PageableHandlerMethodArgumentResolver())
                .build();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @WithMockUser(username = "user", roles = {"ADMIN"})
    public void getAllFromEnv() throws Exception {
        Principal mockPrincipal = Mockito.mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("user");
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/chains/env/1").principal(mockPrincipal);

        Chaine chaine1 = new Chaine();
        chaine1.setId(1L);
        Chaine chaine2 = new Chaine();
        chaine2.setId(2L);

        List<Chaine> chaines = new ArrayList<>();
        chaines.add(chaine1);
        chaines.add(chaine2);

        Page<Chaine> chainePage = new PageImpl<>(chaines);

        when(chaineService.getAllChaineFromEnvironmentWithPageable(any(), any(), argThat((environnement -> environnement.getId().equals(1L))))).thenReturn(chainePage);
        when(permissionEnvironnement.canRead(eq(1L), any())).thenReturn(true);
        restMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[1].id", is(2)));

    }

    @Test
    @WithMockUser(username = "user", roles = {"ADMIN"})
    public void runChaine() throws Exception {
        Principal mockPrincipal = Mockito.mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("user");
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/chains/1/run").principal(mockPrincipal);

        RetourBatch retourBatch = new RetourBatch("noout", 2, 111L);

        when(chaineService.runChaine(eq(1L))).thenReturn(retourBatch);
        when(permissionChaine.canRead(eq(1L), any())).thenReturn(true);
        restMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.returnCode", is(2)))
                .andExpect(jsonPath("$.logOut", is("noout")))
                .andExpect(jsonPath("$.executionTime", is(111)));

    }

    @Test
    @WithMockUser(username = "user", roles = {"ADMIN"})
    public void runChaineError() throws Exception {
        Principal mockPrincipal = Mockito.mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("user");
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/chains/1/run").principal(mockPrincipal);

        RetourBatch retourBatch = new RetourBatch("noout", 2, 111L);

        when(chaineService.runChaine(eq(1L))).thenThrow(new IOException());
        when(permissionChaine.canRead(eq(1L), any())).thenReturn(true);
        restMvc.perform(builder)
                .andExpect(status().isInternalServerError());

    }

    @Test
    @WithMockUser(username = "user", roles = {"ADMIN"})
    public void addChaine() throws Exception {
        Principal mockPrincipal = Mockito.mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("user");

        BatchEnvironnementDto batchEnvironnementDto = new BatchEnvironnementDto();
        batchEnvironnementDto.setId(2L);
        ChaineDto chaineDto = new ChaineDto();
        chaineDto.setDescription("descriptionTest");
        chaineDto.setName("nameTest");
        chaineDto.setEnvironnement(batchEnvironnementDto);

        Chaine chaine = new Chaine();
        chaine.setId(1L);
        chaine.setDescription(chaineDto.getDescription());
        chaine.setName(chaineDto.getName());

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.put("/chains")
                .content(objectMapper.writeValueAsString(chaineDto))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .principal(mockPrincipal);


        when(chaineService.addChaine(argThat((thisChaine) -> chaineDto.getName().equals(chaine.getName())
                && chaineDto.getDescription().equals(thisChaine.getDescription())))).thenReturn(chaine);

        when(permissionEnvironnement.canWrite(eq(2L), any())).thenReturn(true);

        restMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is(chaineDto.getName())))
                .andExpect(jsonPath("$.description", is(chaineDto.getDescription())));
    }

    @Test
    @WithMockUser(username = "user", roles = {"ADMIN"})
    public void removeChaine() throws Exception {
        Principal mockPrincipal = Mockito.mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("user");

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.delete("/chains/1")
                .principal(mockPrincipal);

        when(permissionChaine.canWrite(eq(1L), any())).thenReturn(true);

        restMvc.perform(builder).andExpect(status().isOk());
        verify(chaineService).deleteChaine(eq(1L));

    }


    @Test
    @WithMockUser(username = "user", roles = {"ADMIN"})
    public void updateChaine() throws Exception {
        Principal mockPrincipal = Mockito.mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("user");

        BatchEnvironnementDto batchEnvironnementDto = new BatchEnvironnementDto();
        batchEnvironnementDto.setId(2L);
        ChaineDto chaineDto = new ChaineDto();
        chaineDto.setId(1L);
        chaineDto.setDescription("descriptionTest");
        chaineDto.setName("nameTest");
        chaineDto.setEnvironnement(batchEnvironnementDto);

        Chaine chaine = new Chaine();
        chaine.setId(1L);
        chaine.setDescription(chaineDto.getDescription());
        chaine.setName(chaineDto.getName());

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.patch("/chains")
                .content(objectMapper.writeValueAsString(chaineDto))
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .principal(mockPrincipal);


        when(chaineService.updateChaine(argThat((thisChaine) -> chaineDto.getName().equals(chaine.getName())
                && chaineDto.getDescription().equals(thisChaine.getDescription())))).thenReturn(chaine);

        when(permissionChaine.canWrite(
                (ChaineDto) argThat((thisChainDto) -> chaineDto.getId().equals(((ChaineDto) thisChainDto).getId())),
                any()))
                .thenReturn(true);

        restMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is(chaineDto.getName())))
                .andExpect(jsonPath("$.description", is(chaineDto.getDescription())));
    }
}
