package fr.icdc.ebad.security.permission;

import fr.icdc.ebad.domain.User;
import fr.icdc.ebad.repository.UserRepository;
import fr.icdc.ebad.web.rest.dto.BatchDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by dtrouillet on 01/09/2017.
 */
@Service
public class PermissionBatch  {
    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionBatch.class);

    private final UserRepository userRepository;

    public PermissionBatch(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public boolean canRead(Long batchId, UserDetails userDetails) {
        BatchDto batch = new BatchDto();
        batch.setId(batchId);
        return canRead(batch, userDetails);
    }

    @Transactional(readOnly = true)
    public boolean canWrite(Long batchId, UserDetails userDetails) {
        BatchDto batch = new BatchDto();
        batch.setId(batchId);
        return canWrite(batch, userDetails);
    }

    @Transactional(readOnly = true)
    public boolean canRead(BatchDto batch, UserDetails userDetails) {
        LOGGER.debug("PermissionBatch canRead");
        User user = userRepository.findUserFromBatch(batch.getId(), userDetails.getUsername());
        return user != null;
    }

    @Transactional(readOnly = true)
    public boolean canWrite(BatchDto batch, UserDetails userDetails) {
        LOGGER.debug("PermissionBatch canWrite");
        if (batch.getId() == null) {
            return batch.getEnvironnements().stream().anyMatch(environnement -> userRepository.findManagerFromEnv(environnement.getId(), userDetails.getUsername()) != null);
        }
        User user = userRepository.findManagerFromBatch(batch.getId(), userDetails.getUsername());
        return user != null;
    }
}
