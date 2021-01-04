package fr.icdc.ebad.web.rest;

import fr.icdc.ebad.config.Constants;
import fr.icdc.ebad.domain.Authority;
import fr.icdc.ebad.domain.User;
import fr.icdc.ebad.repository.UserRepository;
import fr.icdc.ebad.service.UserService;
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

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by dtrouillet on 20/08/2019.
 */
@RunWith(SpringRunner.class)
@ActiveProfiles(Constants.SPRING_PROFILE_TEST)
@SpringBootTest
public class AccountResourceTest {
    @Autowired
    private AccountResource accountResource;

    @MockBean
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    private MockMvc restMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.restMvc = MockMvcBuilders.standaloneSetup(accountResource).build();
    }

    @Test
    public void activateTest() throws Exception {
        String key = "codeactivation";
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/activate").param("key", key);

        when(userService.activateRegistration(eq(key))).thenReturn(Optional.of(new User()));

        restMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").doesNotExist());

        verify(userService).activateRegistration(eq(key));
    }

    @Test
    public void activateWithErrorTest() throws Exception {
        String key = "codeactivation";
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/activate").param("key", key);

        when(userService.activateRegistration(eq(key))).thenReturn(Optional.empty());

        restMvc.perform(builder)
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$").doesNotExist());

        verify(userService).activateRegistration(eq(key));
    }

    @Test
    @WithMockUser("testuser")
    public void authenticateTest() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/authenticate");

        restMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(content().string("testuser"));
    }

    @Test
    public void authenticateNoUserTest() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/authenticate");

        restMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    public void accountTest() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/account");

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

        restMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login",is("testlogin")))
                .andExpect(jsonPath("$.password").isEmpty())
                .andExpect(jsonPath("$.roles[0]",is("ROLE_USER")))
                .andExpect(jsonPath("$.email",is("test@test.com")))
                .andExpect(jsonPath("$.firstName",is("testfn")))
                .andExpect(jsonPath("$.lastName",is("testln")))
                .andExpect(jsonPath("$.langKey",is("FR_fr")))
        ;
    }

    @Test
    public void csrfTest() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.get("/csrf");

        restMvc.perform(builder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    public void passwordErrorMinSizeTest() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/account/change_password").content("a");

        restMvc.perform(builder)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void passwordErrorMaxSizeTest() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/account/change_password").content("abcekdjsfghdfkjghdfkjghdfkgjhdfgkjdhfgkdfjghzeuoifgvbdkfjvbzusiohfsdkjfhsziouefhaizuefhizseuhfsdiufhazdfhsdfhsdjfhsdfhosduhfsuidhfizuegfsiudfsoidfoeidfhdofhsdfuoh");

        restMvc.perform(builder)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void passwordErrorEmptyTest() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/account/change_password").content("");

        restMvc.perform(builder)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void passwordErrorNullTest() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/account/change_password");

        restMvc.perform(builder)
                .andExpect(status().isBadRequest());
    }

    @Test
    public void passwordTest() throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.post("/account/change_password").content("newPassword");

        restMvc.perform(builder)
                .andExpect(status().isOk());

        verify(userService).changePassword(eq("newPassword"));
    }
}
