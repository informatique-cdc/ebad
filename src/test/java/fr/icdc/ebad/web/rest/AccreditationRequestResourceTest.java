package fr.icdc.ebad.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.icdc.ebad.config.Constants;
import fr.icdc.ebad.domain.AccreditationRequest;
import fr.icdc.ebad.domain.Application;
import fr.icdc.ebad.domain.User;
import fr.icdc.ebad.service.AccreditationRequestService;
import fr.icdc.ebad.web.rest.dto.CreationAccreditationRequestDto;
import fr.icdc.ebad.web.rest.dto.ResponseAccreditationRequestDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
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
import org.springframework.web.context.WebApplicationContext;

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
public class AccreditationRequestResourceTest {
    @MockBean
    private AccreditationRequestService accreditationRequestService;

    private MockMvc restMvc;

    @Autowired
    private AccreditationRequestResource accreditationRequestResource;

    @Autowired
    private WebApplicationContext context;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.restMvc = MockMvcBuilders
                .standaloneSetup(accreditationRequestResource)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @WithMockUser(username = "user", roles = {"ADMIN"})
    public void findAll() throws Exception {
        List<AccreditationRequest> accreditationRequestList = new ArrayList<>();
        AccreditationRequest accreditationRequest1 = AccreditationRequest
                .builder()
                .id(1L)
                .user(User.builder().id(2L).login("testlogin").build())
                .wantUse(true)
                .wantManage(true)
                .application(Application.builder().id(3L).name("testapp").build())
                .build();

        AccreditationRequest accreditationRequest2 = AccreditationRequest
                .builder()
                .id(4L)
                .user(User.builder().id(2L).login("testlogin").build())
                .wantUse(false)
                .wantManage(true)
                .application(Application.builder().id(3L).name("testapp").build())
                .build();

        accreditationRequestList.add(accreditationRequest1);
        accreditationRequestList.add(accreditationRequest2);

        Page<AccreditationRequest> accreditationRequestPage = new PageImpl<>(accreditationRequestList);

        when(accreditationRequestService.getAllAccreditationRequestToAnswer(any())).thenReturn(accreditationRequestPage);

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/accreditation-requests/need-answer");
        restMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[0].user.login", is("testlogin")))
                .andExpect(jsonPath("$.content[0].application.id", is(3)))
                .andExpect(jsonPath("$.content[0].wantUse", is(true)))
                .andExpect(jsonPath("$.content[0].wantManage", is(true)))
                .andExpect(jsonPath("$.content[1].id", is(4)))
                .andExpect(jsonPath("$.content[1].user.login", is("testlogin")))
                .andExpect(jsonPath("$.content[1].application.id", is(3)))
                .andExpect(jsonPath("$.content[1].wantUse", is(false)))
                .andExpect(jsonPath("$.content[1].wantManage", is(true)));
    }

    @Test
    @WithMockUser(username = "user")
    public void findAllMyRequest() throws Exception {
        List<AccreditationRequest> accreditationRequestList = new ArrayList<>();
        AccreditationRequest accreditationRequest1 = AccreditationRequest
                .builder()
                .id(1L)
                .user(User.builder().id(2L).login("testlogin").build())
                .wantUse(true)
                .wantManage(true)
                .application(Application.builder().id(3L).name("testapp").build())
                .build();

        AccreditationRequest accreditationRequest2 = AccreditationRequest
                .builder()
                .id(4L)
                .user(User.builder().id(2L).login("testlogin").build())
                .wantUse(false)
                .wantManage(true)
                .application(Application.builder().id(3L).name("testapp").build())
                .build();

        accreditationRequestList.add(accreditationRequest1);
        accreditationRequestList.add(accreditationRequest2);

        Page<AccreditationRequest> accreditationRequestPage = new PageImpl<>(accreditationRequestList);

        when(accreditationRequestService.getMyAccreditationRequest(any())).thenReturn(accreditationRequestPage);

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/accreditation-requests");
        restMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[0].user.login", is("testlogin")))
                .andExpect(jsonPath("$.content[0].application.id", is(3)))
                .andExpect(jsonPath("$.content[0].wantUse", is(true)))
                .andExpect(jsonPath("$.content[0].wantManage", is(true)))
                .andExpect(jsonPath("$.content[1].id", is(4)))
                .andExpect(jsonPath("$.content[1].user.login", is("testlogin")))
                .andExpect(jsonPath("$.content[1].application.id", is(3)))
                .andExpect(jsonPath("$.content[1].wantUse", is(false)))
                .andExpect(jsonPath("$.content[1].wantManage", is(true)));
    }

    @Test
    @WithMockUser(username = "user")
    public void createAccreditationRequest() throws Exception {
        CreationAccreditationRequestDto creationAccreditationRequestDto = CreationAccreditationRequestDto.builder().applicationId(1L).wantManage(true).build();
        AccreditationRequest accreditationRequest = AccreditationRequest
                .builder()
                .id(5L)
                .user(User.builder().id(2L).login("testlogin").build())
                .wantUse(false)
                .wantManage(true)
                .application(Application.builder().id(1L).name("testapp").build())
                .build();

        when(accreditationRequestService.requestNewAccreditation(eq(1L), eq(true), eq(false))).thenReturn(accreditationRequest);

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.put("/accreditation-requests")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(creationAccreditationRequestDto));
        restMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(5)))
                .andExpect(jsonPath("$.user.login", is("testlogin")))
                .andExpect(jsonPath("$.application.id", is(1)))
                .andExpect(jsonPath("$.wantUse", is(false)))
                .andExpect(jsonPath("$.wantManage", is(true)));
    }

    @Test
    @WithMockUser(username = "user")
    public void createAccreditationRequest400() throws Exception {
        CreationAccreditationRequestDto creationAccreditationRequestDto = CreationAccreditationRequestDto.builder().wantManage(true).build();

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.put("/accreditation-requests")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(creationAccreditationRequestDto));
        restMvc.perform(builder)
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user", roles = {"ADMIN"})
    public void answerRequest() throws Exception {
        ResponseAccreditationRequestDto responseAccreditationRequestDto = ResponseAccreditationRequestDto.builder().accepted(true).id(1L).build();

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/accreditation-requests/response")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(responseAccreditationRequestDto));
        restMvc.perform(builder)
                .andExpect(status().isOk());

        verify(accreditationRequestService).answerToRequest(eq(1L), eq(true));
    }

    @Test
    @WithMockUser(username = "user", roles = {"ADMIN"})
    public void answerRequest400() throws Exception {
        ResponseAccreditationRequestDto responseAccreditationRequestDto = ResponseAccreditationRequestDto.builder().accepted(true).build();

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/accreditation-requests/response")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(responseAccreditationRequestDto));
        restMvc.perform(builder)
                .andExpect(status().isBadRequest());
    }
}
