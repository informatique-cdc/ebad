package fr.icdc.ebad.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.icdc.ebad.config.Constants;
import fr.icdc.ebad.domain.Batch;
import fr.icdc.ebad.domain.Environnement;
import fr.icdc.ebad.domain.Scheduling;
import fr.icdc.ebad.security.permission.PermissionEnvironnement;
import fr.icdc.ebad.security.permission.PermissionScheduling;
import fr.icdc.ebad.service.SchedulingService;
import fr.icdc.ebad.web.rest.dto.CreationSchedulingDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ActiveProfiles(Constants.SPRING_PROFILE_TEST)
@SpringBootTest
public class SchedulingResourceTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    SchedulingResource schedulingResource;

    @MockBean
    PermissionEnvironnement permissionEnvironnement;

    @MockBean
    PermissionScheduling permissionScheduling;
    @MockBean
    private SchedulingService schedulingService;
    private MockMvc restMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.restMvc = MockMvcBuilders
                .standaloneSetup(schedulingResource)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @WithMockUser
    public void addScheduling() throws Exception {
        String parameters = "testParams";
        String cron = "1 * * * *  ?";

        CreationSchedulingDto creationSchedulingDto = CreationSchedulingDto
                .builder()
                .batchId(1L)
                .environmentId(2L)
                .cron(cron)
                .parameters(parameters)
                .build();

        Batch batch = Batch.builder().id(1L).build();
        Environnement environnement = Environnement.builder().id(2L).build();


        Scheduling scheduling = Scheduling.builder()
                .id(3L)
                .batch(batch)
                .environnement(environnement)
                .parameters(parameters)
                .cron(cron)
                .build();

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .put("/schedulings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(creationSchedulingDto));

        when(permissionEnvironnement.canRead(eq(2L), any())).thenReturn(true);

        when(schedulingService.saveAndRun(1L, 2L, parameters, cron)).thenReturn(scheduling);

        restMvc.perform(builder)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.cron", is(cron)))
                .andExpect(jsonPath("$.batch.id", is(1)))
                .andExpect(jsonPath("$.environnement.id", is(2)))
                .andExpect(jsonPath("$.parameters", is(parameters)));
    }

    @Test
    @WithMockUser
    public void listByEnvironment() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/schedulings/env/2");

        Batch batch = Batch.builder().id(1L).build();
        Environnement environnement = Environnement.builder().id(2L).build();
        String parameters = "testParams";
        String cron1 = "10 * * * * ?";
        String cron2 = "11 * * * * ?";

        Scheduling scheduling1 = Scheduling.builder()
                .id(3L)
                .batch(batch)
                .environnement(environnement)
                .parameters(parameters)
                .cron(cron1)
                .build();

        Scheduling scheduling2 = Scheduling.builder()
                .id(4L)
                .batch(batch)
                .environnement(environnement)
                .parameters(parameters)
                .cron(cron2)
                .build();

        List<Scheduling> schedulings = new ArrayList<>();
        schedulings.add(scheduling1);
        schedulings.add(scheduling2);

        PageImpl<Scheduling> page = new PageImpl<>(schedulings);

        when(permissionEnvironnement.canRead(eq(2L), any())).thenReturn(true);
        when(schedulingService.listByEnvironment(eq(2L), any())).thenReturn(page);
        restMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id", is(3)))
                .andExpect(jsonPath("$.content[0].cron", is(cron1)))
                .andExpect(jsonPath("$.content[0].batch.id", is(1)))
                .andExpect(jsonPath("$.content[0].environnement.id", is(2)))
                .andExpect(jsonPath("$.content[0].parameters", is(parameters)))
                .andExpect(jsonPath("$.content[1].id", is(4)))
                .andExpect(jsonPath("$.content[1].cron", is(cron2)))
                .andExpect(jsonPath("$.content[1].batch.id", is(1)))
                .andExpect(jsonPath("$.content[1].environnement.id", is(2)))
                .andExpect(jsonPath("$.content[1].parameters", is(parameters)));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void listAll() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/schedulings");

        Batch batch = Batch.builder().id(1L).build();
        Environnement environnement = Environnement.builder().id(2L).build();
        String parameters = "testParams";
        String cron1 = "10 * * * * ?";
        String cron2 = "11 * * * * ?";

        Scheduling scheduling1 = Scheduling.builder()
                .id(3L)
                .batch(batch)
                .environnement(environnement)
                .parameters(parameters)
                .cron(cron1)
                .build();

        Scheduling scheduling2 = Scheduling.builder()
                .id(4L)
                .batch(batch)
                .environnement(environnement)
                .parameters(parameters)
                .cron(cron2)
                .build();

        List<Scheduling> schedulings = new ArrayList<>();
        schedulings.add(scheduling1);
        schedulings.add(scheduling2);

        PageImpl<Scheduling> page = new PageImpl<>(schedulings);

        when(schedulingService.listAll(any())).thenReturn(page);
        restMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id", is(3)))
                .andExpect(jsonPath("$.content[0].cron", is(cron1)))
                .andExpect(jsonPath("$.content[0].batch.id", is(1)))
                .andExpect(jsonPath("$.content[0].environnement.id", is(2)))
                .andExpect(jsonPath("$.content[0].parameters", is(parameters)))
                .andExpect(jsonPath("$.content[1].id", is(4)))
                .andExpect(jsonPath("$.content[1].cron", is(cron2)))
                .andExpect(jsonPath("$.content[1].batch.id", is(1)))
                .andExpect(jsonPath("$.content[1].environnement.id", is(2)))
                .andExpect(jsonPath("$.content[1].parameters", is(parameters)));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void get() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/schedulings/1");

        Batch batch = Batch.builder().id(1L).build();
        Environnement environnement = Environnement.builder().id(2L).build();
        String parameters = "testParams";
        String cron = "10 * * * * ?";

        Scheduling scheduling = Scheduling.builder()
                .id(3L)
                .batch(batch)
                .environnement(environnement)
                .parameters(parameters)
                .cron(cron)
                .build();

        when(schedulingService.get(1L)).thenReturn(scheduling);
        when(permissionScheduling.canRead(eq(1L), any())).thenReturn(true);
        restMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.cron", is(cron)))
                .andExpect(jsonPath("$.batch.id", is(1)))
                .andExpect(jsonPath("$.environnement.id", is(2)))
                .andExpect(jsonPath("$.parameters", is(parameters)));
    }

    @Test
    @WithMockUser
    public void delete() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.delete("/schedulings/1");

        when(permissionScheduling.canRead(eq(1L), any())).thenReturn(true);

        restMvc.perform(builder).andExpect(status().isOk());

        verify(schedulingService).remove(1L);
    }
}
