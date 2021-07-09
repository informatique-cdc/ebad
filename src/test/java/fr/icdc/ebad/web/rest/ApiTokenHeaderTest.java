package fr.icdc.ebad.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.icdc.ebad.config.Constants;
import fr.icdc.ebad.domain.User;
import fr.icdc.ebad.service.ApiTokenService;
import fr.icdc.ebad.service.UserService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
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
import org.springframework.web.context.WebApplicationContext;

import java.security.Principal;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ActiveProfiles(Constants.SPRING_PROFILE_TEST)
@SpringBootTest
public class ApiTokenHeaderTest {
    @MockBean
    private ApiTokenService apiTokenService;
    @MockBean
    private UserService userService;
//    @MockBean
//    private EbadUserDetailsService ebadUserDetailsService;


    private MockMvc restMvc;
    private ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Before
    public void setup() {
        this.restMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @Ignore
    @WithMockUser(username = "user", roles = {"ADMIN"})
    public void findToken() throws Exception {
        Principal mockPrincipal = Mockito.mock(Principal.class);
        Mockito.when(mockPrincipal.getName()).thenReturn("user");
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .get("/users/current")
                .principal(mockPrincipal)
                .header("ebad-api-key", "13:mydecodedtoken");

        User user = User.builder()
                .login("test")
                .activated(true)
                .email("test@test.fr")
                .firstName("testFirst")
                .lastName("testLast")
                .password("testPassword")
                .id(1L)
                .build();
        when(apiTokenService.userFromToken(eq("13:mydecodedtoken"))).thenReturn(user);
//        when(ebadUserDetailsService.loadUserByUsername(eq("test"))).thenReturn(user);
        when(userService.getUserWithAuthorities()).thenReturn(user);
        restMvc.perform(builder)
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.login", is("test")))
                .andExpect(jsonPath("$.activated", is(true)))
                .andExpect(jsonPath("$.email", is("test@test.fr")))
                .andExpect(jsonPath("$.firstName", is("testFirst")))
                .andExpect(jsonPath("$.lastName", is("testLast")))
                .andExpect(jsonPath("$.password").doesNotExist());

        verify(apiTokenService).userFromToken(eq("13:mydecodedtoken"));


    }
}
