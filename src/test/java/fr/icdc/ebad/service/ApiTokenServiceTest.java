package fr.icdc.ebad.service;

import fr.icdc.ebad.domain.ApiToken;
import fr.icdc.ebad.domain.User;
import fr.icdc.ebad.repository.ApiTokenRepository;
import fr.icdc.ebad.repository.UserRepository;
import fr.icdc.ebad.service.util.EbadServiceException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ApiTokenServiceTest {
    @InjectMocks
    private ApiTokenService apiTokenService;

    @Mock
    private ApiTokenRepository apiTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    public void findTokenByUser() {
        Pageable pageable = Pageable.ofSize(10);
        List<ApiToken> apiTokens = new ArrayList<>();
        ApiToken apiToken1 = ApiToken.builder().id(1L).build();
        ApiToken apiToken2 = ApiToken.builder().id(2L).build();
        apiTokens.add(apiToken1);
        apiTokens.add(apiToken2);
        Page<ApiToken> apiTokenPage = new PageImpl<>(apiTokens);
        when(apiTokenRepository.findAllByUserLogin(eq("myLogin"), eq(pageable))).thenReturn(apiTokenPage);

        Page<ApiToken> results = apiTokenService.findTokenByUser("myLogin", pageable);

        assertEquals(apiTokenPage, results);
    }

    @Test
    public void createToken() throws EbadServiceException {
        User user = User.builder().id(1L).login("myLogin").build();
        when(userRepository.findOneByLogin(eq("myLogin"))).thenReturn(Optional.of(user));

        ArgumentCaptor<ApiToken> apiTokenArgumentCaptor = ArgumentCaptor.forClass(ApiToken.class);
        ArgumentCaptor<String> tokenArgumentCaptor = ArgumentCaptor.forClass(String.class);
        when(passwordEncoder.encode(tokenArgumentCaptor.capture())).thenReturn("encodedToken");

        ApiToken apiToken = ApiToken.builder().id(52L).token("encodedToken").user(user).name("newToken").build();
        when(apiTokenRepository.save(apiTokenArgumentCaptor.capture())).thenReturn(apiToken);
        ApiToken result = apiTokenService.createToken("myLogin", "newToken");

        assertEquals("52:"+tokenArgumentCaptor.getValue(), result.getToken());
        assertEquals(52L, result.getId(),0);
        assertEquals("newToken", result.getName());
        assertEquals("myLogin", result.getUser().getLogin());
        assertEquals("encodedToken", apiTokenArgumentCaptor.getValue().getToken());
    }

    @Test(expected = EbadServiceException.class)
    public void createTokenError() throws EbadServiceException {
        when(userRepository.findOneByLogin(eq("myLogin"))).thenReturn(Optional.empty());
        apiTokenService.createToken("myLogin", "newToken");
    }


    @Test
    public void deleteToken() {
        apiTokenService.deleteToken(2L);
        verify(apiTokenRepository).deleteById(eq(2L));
    }

    @Test
    public void userFromToken() {
        User user = User.builder().id(2L).login("myLogin").build();
        ApiToken apiToken = ApiToken.builder().token("mytokenencodedInDb").name("myToken").user(user).id(13L).build();
        when(apiTokenRepository.findById(eq(13L))).thenReturn(Optional.of(apiToken));
        when(passwordEncoder.matches(eq("mytokenencoded"), eq("mytokenencodedInDb"))).thenReturn(true);
        User result = apiTokenService.userFromToken("13:mytokenencoded");

        assertEquals(user, result);
    }

    @Test
    public void userFromTokenError() {
        User result = apiTokenService.userFromToken("13mytokenencoded");
        assertNull(result);
    }

    @Test
    public void userFromTokenError2() {
        User result = apiTokenService.userFromToken("13:");
        assertNull(result);
    }

    @Test
    public void userFromTokenError3() {
        User result = apiTokenService.userFromToken("sfdf:sdfsdf");
        assertNull(result);
    }

    @Test
    public void userFromTokenError4() {
        when(apiTokenRepository.findById(eq(13L))).thenReturn(Optional.empty());
        User result = apiTokenService.userFromToken("13:mytokenencoded");

        assertNull(result);
    }

    @Test
    public void userFromToken5() {
        User user = User.builder().id(2L).login("myLogin").build();
        ApiToken apiToken = ApiToken.builder().token("mytokenencodedInDb").name("myToken").user(user).id(13L).build();
        when(apiTokenRepository.findById(eq(13L))).thenReturn(Optional.of(apiToken));
        when(passwordEncoder.matches(eq("mytokenencoded"), eq("mytokenencodedInDb"))).thenReturn(false);
        User result = apiTokenService.userFromToken("13:mytokenencoded");

        assertNull(result);
    }

}
