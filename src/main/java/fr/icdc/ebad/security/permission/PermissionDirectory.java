package fr.icdc.ebad.security.permission;

import fr.icdc.ebad.domain.Directory;
import fr.icdc.ebad.domain.User;
import fr.icdc.ebad.repository.DirectoryRepository;
import fr.icdc.ebad.repository.UserRepository;
import fr.icdc.ebad.web.rest.dto.DirectoryDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by dtrouillet on 01/09/2017.
 */
@Service
@Transactional
public class PermissionDirectory {
    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionDirectory.class);

    private final UserRepository userRepository;
    private final DirectoryRepository directoryRepository;

    public PermissionDirectory(UserRepository userRepository, DirectoryRepository directoryRepository) {
        this.userRepository = userRepository;
        this.directoryRepository = directoryRepository;
    }

    @Transactional(readOnly = true)
    public boolean canRead(Long directoryId, UserDetails userDetails) {
        DirectoryDto directory = new DirectoryDto();
        directory.setId(directoryId);
        return canRead(directory, userDetails);
    }

    @Transactional(readOnly = true)
    public boolean canWriteFile(Long directoryId, UserDetails userDetails) {
        DirectoryDto directory = new DirectoryDto();
        directory.setId(directoryId);
        return canWriteFile(directory, userDetails);
    }

    @Transactional(readOnly = true)
    public boolean canRead(DirectoryDto directory, UserDetails userDetails) {
        LOGGER.debug("PermissionDirectory canRead");
        Directory directoryFromDataBase = directoryRepository.getOne(directory.getId());
        User user = userRepository.findUserFromEnv(directoryFromDataBase.getEnvironnement().getId(), userDetails.getUsername());
        return user != null;
    }

    @Transactional(readOnly = true)
    public boolean canWrite(DirectoryDto directory, UserDetails userDetails) {
        LOGGER.debug("PermissionDirectory canWrite");
        if (directory.getEnvironnement() != null) {
            return userRepository.findManagerFromEnv(directory.getEnvironnement().getId(), userDetails.getUsername()) != null;
        }
        return false;
    }

    @Transactional(readOnly = true)
    public boolean canWriteFile(DirectoryDto directory, UserDetails userDetails) {
        LOGGER.debug("PermissionDirectory canWriteFile");

        Directory directoryFromDataBase = directoryRepository.getOne(directory.getId());
        User user = userRepository.findUserFromEnv(directoryFromDataBase.getEnvironnement().getId(), userDetails.getUsername());

        return user != null && directoryFromDataBase.isCanWrite();
    }
}
