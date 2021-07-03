package fr.icdc.ebad.service;

import fr.icdc.ebad.domain.ApiToken;
import fr.icdc.ebad.domain.User;
import fr.icdc.ebad.repository.ApiTokenRepository;
import fr.icdc.ebad.repository.UserRepository;
import fr.icdc.ebad.service.util.EbadServiceException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;

@Service
public class ApiTokenService {
    private final ApiTokenRepository apiTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder();

    public ApiTokenService(ApiTokenRepository apiTokenRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.apiTokenRepository = apiTokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Page<ApiToken> findTokenByUser(String login, Pageable pageable) {
        return apiTokenRepository.findAllByUserLogin(login, pageable);
    }

    @Transactional
    public ApiToken createToken(String login, String name) throws EbadServiceException {
        Optional<User> optionalUser = userRepository.findOneByLogin(login);
        if (optionalUser.isEmpty()){
            throw new EbadServiceException("No user found");
        }

        String token = generateNewToken();
        ApiToken apiToken = ApiToken
                .builder()
                .user(optionalUser.get())
                .name(name)
                .token(passwordEncoder.encode(token))
                .build();

        ApiToken apiTokenSaved = apiTokenRepository.save(apiToken);
        return ApiToken.builder().id(apiTokenSaved.getId()).name(apiTokenSaved.getName()).token(apiTokenSaved.getId()+":"+token).user(apiTokenSaved.getUser()).build();
    }

    public static String generateNewToken() {
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

    @Transactional
    public void deleteToken(Long id) {
        apiTokenRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public User userFromToken(String apiToken){
        Long id = getIdFromTokenId(apiToken);
        String token = getTokenFromTokenId(apiToken);
        if(token == null || id == null){
            return null;
        }
        Optional<ApiToken> optionalApiToken = apiTokenRepository.findById(id);
        if (optionalApiToken.isEmpty() || !passwordEncoder.matches(token, optionalApiToken.get().getToken())){
            return null;
        }

        return optionalApiToken.get().getUser();
    }

    private String getTokenFromTokenId(String token){
        int index = token.indexOf(":");
        if(index == -1){
            return null;
        }
        return token.substring(index+1);
    }

    private Long getIdFromTokenId(String token){
        int index = token.indexOf(":");
        if(index == -1){
            return null;
        }

        try {
            return Long.valueOf(token.substring(0, index));
        }catch (NumberFormatException e){
            return null;
        }
    }
}
