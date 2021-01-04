package fr.icdc.ebad.security.permission;

import fr.icdc.ebad.domain.User;
import fr.icdc.ebad.repository.UserRepository;
import fr.icdc.ebad.web.rest.dto.EnvironnementDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by dtrouillet on 01/09/2017.
 */
@Service
public class PermissionEnvironnement {
    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionEnvironnement.class);

    private final UserRepository userRepository;

    public PermissionEnvironnement(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public boolean canRead(Long environnementId, UserDetails userDetails) {
        EnvironnementDto environnement = new EnvironnementDto();
        environnement.setId(environnementId);
        return canRead(environnement, userDetails);
    }

    @Transactional(readOnly = true)
    public boolean canWrite(Long environnementId, UserDetails userDetails) {
        EnvironnementDto environnement = new EnvironnementDto();
        environnement.setId(environnementId);
        return canWrite(environnement, userDetails);
    }

    @Transactional(readOnly = true)
    public boolean canRead(EnvironnementDto environnement, UserDetails userDetails) {
        LOGGER.debug("PermissionEnvironnement canRead");
        if (environnement == null || environnement.getId() == null) {
            return false;
        }
        User user = userRepository.findUserFromEnv(environnement.getId(), userDetails.getUsername());
        return user != null;
    }

    @Transactional(readOnly = true)
    public boolean canWrite(EnvironnementDto environnement, UserDetails userDetails) {
        LOGGER.debug("PermissionEnvironnement canWrite");
        if (environnement == null || environnement.getId() == null) {
            return false;
        }
        return userRepository.findManagerFromEnv(environnement.getId(), userDetails.getUsername()) != null;
    }
}
