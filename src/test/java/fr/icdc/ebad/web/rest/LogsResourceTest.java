package fr.icdc.ebad.web.rest;

import fr.icdc.ebad.config.Constants;
import fr.icdc.ebad.domain.LogBatch;
import fr.icdc.ebad.security.permission.PermissionEnvironnement;
import fr.icdc.ebad.service.LogBatchService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

import java.security.Principal;
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
public class LogsResourceTest {
    @Autowired
    private LogsResource logsResource;

    @MockBean
    private LogBatchService logBatchService;

    @MockBean
    private PermissionEnvironnement permissionEnvironnement;

    @Autowired
    private QuerydslPredicateArgumentResolver querydslPredicateArgumentResolver;

    private MockMvc restMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.restMvc = MockMvcBuilders
                .standaloneSetup(logsResource)
                .setCustomArgumentResolvers(querydslPredicateArgumentResolver, new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @WithMockUser(username = "user", roles = {"ADMIN"})
    public void getAllLog() throws Exception {
        Principal mockPrincipal = Mockito.mock(Principal.class);
        Mockito.when(mockPrincipal.getName()).thenReturn("user");
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/logs").principal(mockPrincipal);

        List<LogBatch> logBatches = new ArrayList<>();
        LogBatch logBatch1 = new LogBatch();
        logBatch1.setId(1L);
        LogBatch logBatch2 = new LogBatch();
        logBatch2.setId(2L);

        logBatches.add(logBatch1);
        logBatches.add(logBatch2);

        PageImpl<LogBatch> logBatchPage = new PageImpl<>(logBatches);

        when(logBatchService.getAllLogBatchWithPageable(any(), any())).thenReturn(logBatchPage);

        restMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[1].id", is(2)));
    }

    @Test
    @WithMockUser(username = "user", roles = {"ADMIN"})
    public void getAllLogFromEnv() throws Exception {
        Principal mockPrincipal = Mockito.mock(Principal.class);
        Mockito.when(mockPrincipal.getName()).thenReturn("user");
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/logs/1").principal(mockPrincipal);

        List<LogBatch> logBatches = new ArrayList<>();
        LogBatch logBatch1 = new LogBatch();
        logBatch1.setId(1L);
        LogBatch logBatch2 = new LogBatch();
        logBatch2.setId(2L);

        logBatches.add(logBatch1);
        logBatches.add(logBatch2);

        PageImpl<LogBatch> logBatchPage = new PageImpl<>(logBatches);

        when(logBatchService.getAllLogBatchWithPageable(any(), any())).thenReturn(logBatchPage);
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
    public void getAllLogFromEnvBatch() throws Exception {
        Principal mockPrincipal = Mockito.mock(Principal.class);
        Mockito.when(mockPrincipal.getName()).thenReturn("user");
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/logs/1/2").principal(mockPrincipal);

        List<LogBatch> logBatches = new ArrayList<>();
        LogBatch logBatch1 = new LogBatch();
        logBatch1.setId(1L);
        LogBatch logBatch2 = new LogBatch();
        logBatch2.setId(2L);

        logBatches.add(logBatch1);
        logBatches.add(logBatch2);

        PageImpl<LogBatch> logBatchPage = new PageImpl<>(logBatches);

        when(logBatchService.getAllLogBatchWithPageable(any(), any())).thenReturn(logBatchPage);
        when(permissionEnvironnement.canRead(eq(1L), any())).thenReturn(true);

        restMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[1].id", is(2)));
    }
}
