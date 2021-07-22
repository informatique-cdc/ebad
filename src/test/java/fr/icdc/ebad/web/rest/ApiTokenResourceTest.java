package fr.icdc.ebad.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.icdc.ebad.config.Constants;
import fr.icdc.ebad.domain.ApiToken;
import fr.icdc.ebad.security.permission.PermissionApiKey;
import fr.icdc.ebad.service.ApiTokenService;
import fr.icdc.ebad.web.rest.dto.ApiTokenDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ActiveProfiles(Constants.SPRING_PROFILE_TEST)
@SpringBootTest
public class ApiTokenResourceTest {
    @Autowired
    private ApiTokenResource apiTokenResource;
    @MockBean
    private ApiTokenService apiTokenService;
    @MockBean
    private PermissionApiKey permissionApiKey;

    private MockMvc restMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setup() {
        this.restMvc = MockMvcBuilders
                .standaloneSetup(apiTokenResource)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @WithMockUser(username = "user", roles = {"ADMIN"})
    public void findToken() throws Exception {
        Principal mockPrincipal = Mockito.mock(Principal.class);
        Mockito.when(mockPrincipal.getName()).thenReturn("user");
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/api-tokens").principal(mockPrincipal);

        List<ApiToken> apiTokens = new ArrayList<>();
        ApiToken apiToken1 = ApiToken.builder().id(1L).build();
        ApiToken apiToken2 = ApiToken.builder().id(2L).build();
        apiTokens.add(apiToken1);
        apiTokens.add(apiToken2);
        Page<ApiToken> apiTokenPage = new PageImpl<>(apiTokens);
        when(apiTokenService.findTokenByUser(eq("user"), ArgumentMatchers.any(Pageable.class))).thenReturn(apiTokenPage);

        restMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[0].token").doesNotExist())
                .andExpect(jsonPath("$.content[1].id", is(2)))
                .andExpect(jsonPath("$.content[1].token").doesNotExist());
    }

    @Test
    @WithMockUser(username = "user", roles = {"ADMIN"})
    public void createToken() throws Exception {
        Principal mockPrincipal = Mockito.mock(Principal.class);
        Mockito.when(mockPrincipal.getName()).thenReturn("user");
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.put("/api-tokens").principal(mockPrincipal);
        ApiTokenDto apiTokenDto = new ApiTokenDto();
        apiTokenDto.setName("newToken");
        ApiToken apiToken = ApiToken.builder().id(1L).name("newToken").token("13:newtokendecoded").build();

        when(apiTokenService.createToken(eq("user"),eq("newToken"))).thenReturn(apiToken);

        restMvc.perform(builder.content(objectMapper.writeValueAsString(apiTokenDto)).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.token", is("13:newtokendecoded")))
                .andExpect(jsonPath("$.name", is("newToken")));
    }

    @Test
    @WithMockUser(username = "user", roles = {"ADMIN"})
    public void deleteToken() throws Exception {
        Principal mockPrincipal = Mockito.mock(Principal.class);
        Mockito.when(mockPrincipal.getName()).thenReturn("user");
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.delete("/api-tokens/1").principal(mockPrincipal);

        when(permissionApiKey.canReadWrite(eq(1L),ArgumentMatchers.any(UserDetails.class))).thenReturn(true);

        restMvc.perform(builder)
                .andExpect(status().isOk());

        verify(apiTokenService).deleteToken(eq(1L));

    }
}
