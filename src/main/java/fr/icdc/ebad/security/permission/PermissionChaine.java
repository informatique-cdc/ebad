package fr.icdc.ebad.security.permission;

import fr.icdc.ebad.domain.Chaine;
import fr.icdc.ebad.domain.User;
import fr.icdc.ebad.repository.ChaineRepository;
import fr.icdc.ebad.repository.EnvironnementRepository;
import fr.icdc.ebad.repository.UserRepository;
import fr.icdc.ebad.web.rest.dto.ChaineDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by dtrouillet on 01/09/2017.
 */
@Service
public class PermissionChaine{
    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionChaine.class);

    private final UserRepository userRepository;
    private final ChaineRepository chaineRepository;

    public PermissionChaine(UserRepository userRepository, ChaineRepository chaineRepository, EnvironnementRepository environnementRepository) {
        this.userRepository = userRepository;
        this.chaineRepository = chaineRepository;
    }

    @Transactional(readOnly = true)
    public boolean canRead(Long chaineId, UserDetails userDetails) {
        ChaineDto chaineDto = new ChaineDto();
        chaineDto.setId(chaineId);
        return canRead(chaineDto, userDetails);
    }

    @Transactional(readOnly = true)
    public boolean canWrite(Long chaineId, UserDetails userDetails) {
        ChaineDto chaineDto = new ChaineDto();
        chaineDto.setId(chaineId);
        return canWrite(chaineDto, userDetails);
    }

    @Transactional(readOnly = true)
    public boolean canRead(ChaineDto chaineDto, UserDetails userDetails) {
        LOGGER.debug("PermissionChaine canRead");
        Chaine chaineFromDatabase = chaineRepository.getOne(chaineDto.getId());
        User user = userRepository.findUserFromEnv(chaineFromDatabase.getEnvironnement().getId(), userDetails.getUsername());
        return user != null;
    }

    @Transactional(readOnly = true)
    public boolean canWrite(ChaineDto chaineDto, UserDetails userDetails) {
        LOGGER.debug("PermissionChaine canWrite");
        if (chaineDto.getId() == null) {
            return userRepository.findManagerFromEnv(chaineDto.getEnvironnement().getId(), userDetails.getUsername()) != null;
        }
        Chaine chaineFromDatabase = chaineRepository.getOne(chaineDto.getId());
        User user = userRepository.findManagerFromEnv(chaineFromDatabase.getEnvironnement().getId(), userDetails.getUsername());
        return user != null;
    }
}
