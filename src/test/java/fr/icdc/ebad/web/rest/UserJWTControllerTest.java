package fr.icdc.ebad.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.icdc.ebad.config.Constants;
import fr.icdc.ebad.domain.Authority;
import fr.icdc.ebad.domain.User;
import fr.icdc.ebad.security.jwt.TokenProvider;
import fr.icdc.ebad.service.UserService;
import fr.icdc.ebad.web.rest.dto.LoginDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@ActiveProfiles({Constants.SPRING_PROFILE_TEST, "jwt"})
@SpringBootTest
public class UserJWTControllerTest {
    @MockBean
    private TokenProvider tokenProvider;

    @MockBean(name = "customAuthenticationManager")
    private AuthenticationManager customAuthenticationManager;

    @MockBean
    private UserService userService;

    @Autowired
    private UserJWTController userJWTController;

    private MockMvc restMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.restMvc = MockMvcBuilders.standaloneSetup(userJWTController).build();
    }

    @Test
    public void authorize() throws Exception {
        LoginDto loginDto = new LoginDto();
        loginDto.setUsername("admin");
        loginDto.setPassword("password");

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .post("/authenticate")
                .content(new ObjectMapper().writeValueAsString(loginDto))
                .contentType(MediaType.APPLICATION_JSON_UTF8);


        Set<Authority> authorities = new HashSet<>();
        authorities.add(new Authority("ROLE_USER", new HashSet<>()));

        User user = User.builder()
                .activated(true)
                .id(1L)
                .login("testlogin")
                .password("password")
                .authorities(authorities)
                .email("test@test.com")
                .firstName("testfn")
                .langKey("FR_fr")
                .lastName("testln")
                .build();

        when(userService.getUserWithAuthorities()).thenReturn(user);
        when(tokenProvider.createToken(any(), anyBoolean())).thenReturn("thisawesometoken");

        restMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login", is("testlogin")))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.authorities[0].name", is("ROLE_USER")))
                .andExpect(jsonPath("$.email", is("test@test.com")))
                .andExpect(jsonPath("$.firstName", is("testfn")))
                .andExpect(jsonPath("$.lastName", is("testln")))
                .andExpect(jsonPath("$.langKey", is("FR_fr")))
                .andExpect(header().string("Authorization", "Bearer thisawesometoken"));
    }
}
