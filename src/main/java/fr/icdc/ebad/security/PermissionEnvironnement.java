package fr.icdc.ebad.security;

import fr.icdc.ebad.domain.User;
import fr.icdc.ebad.repository.UserRepository;
import fr.icdc.ebad.web.rest.dto.BatchEnvironnementDto;
import fr.icdc.ebad.web.rest.dto.EnvironnementDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        User user = userRepository.findUserFromEnv(environnement.getId(),userDetails.getUsername());
        return user != null;
    }

    @Transactional(readOnly = true)
    public boolean canWrite(EnvironnementDto environnementDto, UserDetails userDetails) {
        LOGGER.debug("PermissionEnvironnement canWrite");
        if (environnementDto.getId() == null) {
            return false;
        }
        return userRepository.findManagerFromEnv(environnementDto.getId(), userDetails.getUsername()) != null;
    }

    @Transactional(readOnly = true)
    public boolean canWriteEnvironnements(List<BatchEnvironnementDto> environnements, UserDetails userDetails) {
        LOGGER.debug("PermissionEnvironnement canWrite listEnvironnement");
        for (BatchEnvironnementDto environnement : environnements) {
            if (canWrite(environnement.getId(), userDetails)) {
                return true;
            }
        }
        return false;
    }

    @Transactional(readOnly = true)
    public boolean canReadEnvironnements(List<BatchEnvironnementDto> environnements, UserDetails userDetails) {
        LOGGER.debug("PermissionEnvironnement canWrite listEnvironnement");
        for (BatchEnvironnementDto environnement : environnements) {
            if (canRead(environnement.getId(), userDetails)) {
                return true;
            }
        }
        return false;
    }

}
