package fr.icdc.ebad.service;

import fr.icdc.ebad.domain.ApiToken;
import fr.icdc.ebad.domain.User;
import fr.icdc.ebad.repository.ApiTokenRepository;
import fr.icdc.ebad.repository.UserRepository;
import fr.icdc.ebad.service.util.EbadServiceException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;

@Service
public class ApiTokenService {
    private final ApiTokenRepository apiTokenRepository;
    private final UserRepository userRepository;

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder();

    public ApiTokenService(ApiTokenRepository apiTokenRepository, UserRepository userRepository) {
        this.apiTokenRepository = apiTokenRepository;
        this.userRepository = userRepository;
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
        ApiToken apiToken = ApiToken
                .builder()
                .user(optionalUser.get())
                .name(name)
                .token(generateNewToken())
                .build();

        return apiTokenRepository.save(apiToken);
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
    public User userFromToken(String login, String apiToken){
        Optional<ApiToken> optionalApiToken = apiTokenRepository.findApiTokenByUserLoginAndToken(login, apiToken);
        if (optionalApiToken.isEmpty()){
            return null;
        }

        return optionalApiToken.get().getUser();
    }
}
