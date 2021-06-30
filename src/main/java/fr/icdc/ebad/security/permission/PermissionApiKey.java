package fr.icdc.ebad.security.permission;

import fr.icdc.ebad.domain.ApiToken;
import fr.icdc.ebad.repository.ApiTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PermissionApiKey {
    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionApiKey.class);

    private final ApiTokenRepository apiTokenRepository;

    public PermissionApiKey(ApiTokenRepository apiTokenRepository) {
        this.apiTokenRepository = apiTokenRepository;
    }


    public boolean canReadWrite(Long apiTokenId, UserDetails userDetails) {
        LOGGER.debug("PermissionApiKey canReadWrite");
        if (apiTokenId == null) {
            return false;
        }
        Optional<ApiToken> optionalApiToken = apiTokenRepository.findById(apiTokenId);
        if(optionalApiToken.isEmpty()){
            return false;
        }
        return optionalApiToken.get().getUser().getLogin().equals(userDetails.getUsername());
    }

}
