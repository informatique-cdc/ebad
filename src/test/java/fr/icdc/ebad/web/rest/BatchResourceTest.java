package fr.icdc.ebad.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jcraft.jsch.JSchException;
import fr.icdc.ebad.config.Constants;
import fr.icdc.ebad.domain.Batch;
import fr.icdc.ebad.domain.Environnement;
import fr.icdc.ebad.domain.util.RetourBatch;
import fr.icdc.ebad.security.permission.PermissionBatch;
import fr.icdc.ebad.security.permission.PermissionEnvironnement;
import fr.icdc.ebad.service.BatchService;
import fr.icdc.ebad.web.rest.dto.BatchDto;
import fr.icdc.ebad.web.rest.dto.BatchEnvironnementDto;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by dtrouillet on 06/03/2018.
 */
@RunWith(SpringRunner.class)
@ActiveProfiles(Constants.SPRING_PROFILE_TEST)
@SpringBootTest
public class BatchResourceTest {
    @MockBean
    BatchService batchService;

    @MockBean
    PermissionEnvironnement permissionEnvironnement;
    @MockBean
    PermissionBatch permissionBatch;

    @Autowired
    BatchResource batchResource;

    private MockMvc restMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.restMvc = MockMvcBuilders.standaloneSetup(batchResource).build();
    }

    @Test
    @Ignore("Use DBUNIT to test gracefully predicate")
    @WithMockUser
    public void getAllFromEnv() throws Exception {
        List<Batch> batchList = new ArrayList<>();
        Batch batch1 = new Batch();
        batch1.setId(1L);
        batchList.add(batch1);
        Batch batch2 = new Batch();
        batch2.setId(2L);
        batchList.add(batch2);
        Page page = new PageImpl(batchList);
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/batchs/env/1");
        when(permissionEnvironnement.canRead(eq(1L), any())).thenReturn(true);
//        when(batchService.getAllBatchWithPredicate(argThat(), any())).thenReturn(page);

        restMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));
    }

    @Test
    @WithMockUser
    public void runBatch() throws Exception {
        RetourBatch retourBatch = new RetourBatch();
        retourBatch.setLogOut("this is ok");
        retourBatch.setReturnCode(0);
        retourBatch.setExecutionTime(123L);

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/batchs/run/1?env=2");
        when(permissionEnvironnement.canRead(eq(2L), any())).thenReturn(true);
        when(batchService.runBatch(eq(1L), eq(2L), eq(null))).thenReturn(retourBatch);

        restMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.logOut", is("this is ok")))
                .andExpect(jsonPath("$.returnCode", is(0)))
                .andExpect(jsonPath("$.executionTime", is(123)));
    }


    @Test
    @WithMockUser
    public void runBatchError() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/batchs/run/1?env=2");
        when(permissionEnvironnement.canRead(eq(2L), any())).thenReturn(true);
        when(batchService.runBatch(eq(1L), eq(2L), eq(null))).thenThrow(new JSchException());

        restMvc.perform(builder)
                .andExpect(status().is5xxServerError());
    }

    @Test
    @WithMockUser
    public void addBatch() throws Exception {
        BatchDto batchDto = new BatchDto();
        batchDto.setDefaultParam("defaultParam");
        BatchEnvironnementDto batchEnvironnementDto = new BatchEnvironnementDto();
        batchEnvironnementDto.setId(2L);

        Set<BatchEnvironnementDto> batchEnvironnementSet = new HashSet<>();
        batchEnvironnementSet.add(batchEnvironnementDto);

        batchDto.setEnvironnements(batchEnvironnementSet);
        batchDto.setName("Batch test");
        batchDto.setPath("test.ksh");
        batchDto.setCreatedDate(LocalDateTime.now());

        Set<Environnement> environnements = new HashSet<>();
        environnements.add(Environnement.builder().name("envtest").id(2L).build());

        Batch batch = new Batch();
        batch.setId(1L);
        batch.setDefaultParam(batchDto.getParams());
        batch.setName(batchDto.getName());
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        batch.setPath(batchDto.getPath());
        batch.setEnvironnements(environnements);
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .put("/batchs")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(batchDto));
        when(permissionBatch.canWrite(any(BatchDto.class), any())).thenReturn(true);
        when(batchService.saveBatch(argThat((argBatch ->
                null == argBatch.getId() &&
                        argBatch.getDefaultParam().equals("defaultParam") &&
                        1 == argBatch.getEnvironnements().size() &&
                        argBatch.getName().equals("Batch test") &&
                        argBatch.getPath().equals("test.ksh")
        )))).thenReturn(batch);

        restMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Batch test")))
                .andExpect(jsonPath("$.environnements[0].id", is(2)))
                .andExpect(jsonPath("$.environnements[0].name", is("envtest")))
                .andExpect(jsonPath("$.path", is("test.ksh")));
    }

    @Test
    @WithMockUser
    public void removeBatch() throws Exception {
        BatchDto batchDto = new BatchDto();
        batchDto.setId(2L);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/batchs/delete/1")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(batchDto));
        when(permissionBatch.canWrite(any(BatchDto.class), any())).thenReturn(true);
        doNothing().when(batchService).deleteBatch(eq(2L));

        restMvc.perform(builder)
                .andExpect(status().isOk());
    }

}
