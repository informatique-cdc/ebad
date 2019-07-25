package fr.icdc.ebad.web.rest;

import fr.icdc.ebad.config.Constants;
import fr.icdc.ebad.domain.Norme;
import fr.icdc.ebad.repository.NormeRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
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
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ActiveProfiles(Constants.SPRING_PROFILE_TEST)
@SpringBootTest
public class NormeResourceTest {
    @Autowired
    private NormeResource normeResource;

    @MockBean
    private NormeRepository normeRepository;

    private MockMvc restMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.restMvc = MockMvcBuilders.standaloneSetup(normeResource).build();
    }

    @Test
    @WithMockUser(roles = {"USER","ADMIN"})
    public void getAll() throws Exception {
        List<Norme> normeList = new ArrayList<>();

        Norme norme1 = new Norme();
        norme1.setId(1L);

        Norme norme2 = new Norme();
        norme2.setId(2L);

        normeList.add(norme1);
        normeList.add(norme2);

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/api/normes");

        when(normeRepository.findAll(any(Sort.class))).thenReturn(normeList);

        restMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$",hasSize(2)))
                .andExpect(jsonPath("$[0].id",is(1)))
                .andExpect(jsonPath("$[1].id",is(2)))
                .andDo(print());

        verify(normeRepository,only()).findAll(any(Sort.class));
    }

    @Test
    public void getOne() throws Exception {

    }

    @Test
    public void save() throws Exception {

    }

    @Test
    public void delete() throws Exception {

    }

    @Test
    public void update() throws Exception {

    }

}
