package fr.icdc.ebad.web.rest;

import com.querydsl.core.types.Predicate;
import fr.icdc.ebad.config.Constants;
import fr.icdc.ebad.domain.Norme;
import fr.icdc.ebad.service.NormeService;
import fr.icdc.ebad.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ActiveProfiles(Constants.SPRING_PROFILE_TEST)
@SpringBootTest
public class NormResourceTest {
    @Autowired
    private NormResource normResource;

    @MockBean
    private NormeService normeService;

    private MockMvc restMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.restMvc = MockMvcBuilders
                .standaloneSetup(normResource)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void getAll() throws Exception {
        List<Norme> normeList = new ArrayList<>();

        Norme norme1 = new Norme();
        norme1.setId(1L);

        Norme norme2 = new Norme();
        norme2.setId(2L);

        normeList.add(norme1);
        normeList.add(norme2);
        Page<Norme> normePage = new PageImpl<>(normeList);
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/norms");

        when(normeService.getAllNormes(any(Predicate.class), any(Pageable.class))).thenReturn(normePage);

        restMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[1].id", is(2)));

        verify(normeService, only()).getAllNormes(any(Predicate.class), any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    public void getAllList() throws Exception {
        List<Norme> normeList = new ArrayList<>();

        Norme norme1 = new Norme();
        norme1.setId(1L);

        Norme norme2 = new Norme();
        norme2.setId(2L);

        normeList.add(norme1);
        normeList.add(norme2);
        Page<Norme> normePage = new PageImpl<>(normeList);
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/norms/name");

        when(normeService.getAllNormes(any(Predicate.class), any(Pageable.class))).thenReturn(normePage);

        restMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[1].id", is(2)));

        verify(normeService, only()).getAllNormes(any(Predicate.class), any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void getOne() throws Exception {

        Norme norme1 = new Norme();
        norme1.setId(1L);
        norme1.setPathShell("/test");
        norme1.setName("nameNorme");
        norme1.setCommandLine("/bin/bash");
        norme1.setCtrlMDate("date.txt");

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/norms/1");

        when(normeService.findNormeById(eq(1L))).thenReturn(Optional.of(norme1));

        restMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is(norme1.getName())))
                .andExpect(jsonPath("$.pathShell", is(norme1.getPathShell())))
                .andExpect(jsonPath("$.commandLine", is(norme1.getCommandLine())))
                .andExpect(jsonPath("$.ctrlMDate", is(norme1.getCtrlMDate())));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void save() throws Exception {
        Norme norme = Norme.builder()
                .pathShell("/test")
                .name("nameNorme")
                .commandLine("/bin/bash")
                .ctrlMDate("date.txt")
                .build();

        Norme normWithId = Norme.builder()
                .id(1L)
                .pathShell("/test")
                .name("nameNorme")
                .commandLine("/bin/bash")
                .ctrlMDate("date.txt")
                .build();

        when(normeService.saveNorme(argThat(thatNorm ->
                norme.getPathShell().equals(thatNorm.getPathShell())
                        && norme.getCtrlMDate().equals(thatNorm.getCtrlMDate())
                        && norme.getName().equals(thatNorm.getName())
                        && norme.getCommandLine().equals(thatNorm.getCommandLine())
        ))).thenReturn(normWithId);

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.put("/norms");
        restMvc.perform(
                builder
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(norme)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is(normWithId.getName())))
                .andExpect(jsonPath("$.pathShell", is(normWithId.getPathShell())))
                .andExpect(jsonPath("$.commandLine", is(normWithId.getCommandLine())))
                .andExpect(jsonPath("$.ctrlMDate", is(normWithId.getCtrlMDate())));

        verify(normeService).saveNorme(eq(norme));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void saveError() throws Exception {
        Norme norme = Norme.builder()
                .id(1L)
                .build();

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.put("/norms");
        restMvc.perform(
                builder
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(norme)))
                .andExpect(status().isBadRequest());

    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void delete() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.delete("/norms/1");
        restMvc.perform(
                builder)
                .andExpect(status().isOk());

        verify(normeService).deleteNormeById(eq(1L));

    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void update() throws Exception {
        Norme norme = Norme.builder()
                .id(1L)
                .pathShell("/test")
                .name("nameNorme")
                .commandLine("/bin/bash")
                .ctrlMDate("date.txt")
                .build();


        when(normeService.saveNorme(argThat(thatNorm ->
                norme.getPathShell().equals(thatNorm.getPathShell())
                        && norme.getCtrlMDate().equals(thatNorm.getCtrlMDate())
                        && norme.getName().equals(thatNorm.getName())
                        && norme.getCommandLine().equals(thatNorm.getCommandLine())
        ))).thenReturn(norme);

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.patch("/norms");
        restMvc.perform(
                builder
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(norme)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is(norme.getName())))
                .andExpect(jsonPath("$.pathShell", is(norme.getPathShell())))
                .andExpect(jsonPath("$.commandLine", is(norme.getCommandLine())))
                .andExpect(jsonPath("$.ctrlMDate", is(norme.getCtrlMDate())));

        verify(normeService).saveNorme(eq(norme));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void updateError() throws Exception {
        Norme norme = Norme.builder()
                .build();

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.patch("/norms");
        restMvc.perform(
                builder
                        .contentType(TestUtil.APPLICATION_JSON_UTF8)
                        .content(TestUtil.convertObjectToJsonBytes(norme)))
                .andExpect(status().isBadRequest());
    }

}
