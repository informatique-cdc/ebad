package fr.icdc.ebad.web.rest;

import fr.icdc.ebad.config.Constants;
import fr.icdc.ebad.domain.Actualite;
import fr.icdc.ebad.service.ActualiteService;
import fr.icdc.ebad.util.TestUtil;
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
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by dtrouillet on 21/03/2018.
 */
@RunWith(SpringRunner.class)
@ActiveProfiles(Constants.SPRING_PROFILE_TEST)
@SpringBootTest
public class ActualiteResourceTest {
    @Autowired
    private ActualiteResource actualiteResource;

    @MockBean
    private ActualiteService actualiteService;

    private MockMvc restMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.restMvc = MockMvcBuilders.standaloneSetup(actualiteResource).build();
    }


    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void getAll() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/api/actualites");

        List<Actualite> actualites = new ArrayList<>();
        Actualite actualite1 = new Actualite();
        actualite1.setId(1L);
        actualites.add(actualite1);

        Actualite actualite2 = new Actualite();
        actualite2.setId(2L);
        actualites.add(actualite2);

        when(actualiteService.getAllActualites()).thenReturn(actualites);

        restMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    public void getAllNonDraft() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/api/actualites/public");

        List<Actualite> actualites = new ArrayList<>();
        Actualite actualite1 = new Actualite();
        actualite1.setId(1L);
        actualites.add(actualite1);

        Actualite actualite2 = new Actualite();
        actualite2.setId(2L);
        actualites.add(actualite2);

        when(actualiteService.getAllActualitesPubliees()).thenReturn(actualites);

        restMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    public void getOne() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/api/actualites/1");

        Actualite actualite1 = new Actualite();
        actualite1.setId(1L);


        when(actualiteService.getActualite(eq(1L))).thenReturn(Optional.of(actualite1));

        restMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void save() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.put("/api/actualites");

        Actualite actualite1 = new Actualite();
        actualite1.setTitle("test");
        actualite1.setContent("content");
        actualite1.setCreatedDate(null);
        actualite1.setLastModifiedDate(null);

        when(actualiteService.saveActualite(eq(actualite1))).thenReturn(actualite1);

        restMvc.perform(
                builder
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(actualite1)))
                .andExpect(status().isOk());

        verify(actualiteService, times(1)).saveActualite(eq(actualite1));

    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void saveError() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.put("/api/actualites");

        Actualite actualite1 = new Actualite();
        actualite1.setId(1L);
        actualite1.setTitle("test");
        actualite1.setContent("content");
        actualite1.setCreatedDate(null);
        actualite1.setLastModifiedDate(null);

        restMvc.perform(
                builder
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(actualite1)))
                .andExpect(status().isBadRequest());

        verify(actualiteService, times(0)).saveActualite(eq(actualite1));

    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void delete() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.delete("/api/actualites/1");

        doNothing().when(actualiteService).deleteActualite(argThat(actualite -> actualite.getId() == 1L));

        restMvc.perform(
                builder.contentType(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        verify(actualiteService, times(1)).deleteActualite(argThat(actualite -> actualite.getId() == 1L));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void update() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.patch("/api/actualites");

        Actualite actualite1 = new Actualite();
        actualite1.setId(1L);
        actualite1.setTitle("test");
        actualite1.setContent("content");
        actualite1.setCreatedDate(null);
        actualite1.setLastModifiedDate(null);


        when(actualiteService.saveActualite(eq(actualite1))).thenReturn(actualite1);

        restMvc.perform(
                builder
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(actualite1)))
                .andExpect(status().isOk());

        verify(actualiteService, times(1)).saveActualite(eq(actualite1));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void updateError() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.patch("/api/actualites");

        Actualite actualite1 = new Actualite();
        actualite1.setTitle("test");
        actualite1.setContent("content");
        actualite1.setCreatedDate(null);
        actualite1.setLastModifiedDate(null);

        restMvc.perform(
                builder
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(actualite1)))
                .andExpect(status().isBadRequest());

        verify(actualiteService, times(0)).saveActualite(eq(actualite1));
    }

}
