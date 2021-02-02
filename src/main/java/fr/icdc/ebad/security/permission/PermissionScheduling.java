package fr.icdc.ebad.security.permission;

import fr.icdc.ebad.domain.Scheduling;
import fr.icdc.ebad.domain.User;
import fr.icdc.ebad.repository.SchedulingRepository;
import fr.icdc.ebad.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Created by dtrouillet on 01/09/2017.
 */
@Service
@Transactional
public class PermissionScheduling {
    private final UserRepository userRepository;
    private final SchedulingRepository schedulingRepository;

    public PermissionScheduling(UserRepository userRepository, SchedulingRepository schedulingRepository) {
        this.userRepository = userRepository;
        this.schedulingRepository = schedulingRepository;
    }

    @Transactional(readOnly = true)
    public boolean canRead(Long schedulignId, UserDetails userDetails) {
        Optional<Scheduling> scheduling = schedulingRepository.findById(schedulignId);
        if (scheduling.isEmpty()) {
            return false;
        }
        User user = userRepository.findUserFromEnv(scheduling.get().getEnvironnement().getId(), userDetails.getUsername());
        return user != null;
    }
}
