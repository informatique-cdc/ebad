package fr.icdc.ebad.security.permission;

import fr.icdc.ebad.config.Constants;
import fr.icdc.ebad.domain.User;
import fr.icdc.ebad.repository.UserRepository;
import fr.icdc.ebad.web.rest.dto.ApplicationDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by dtrouillet on 01/09/2017.
 */
@Service
public class PermissionApplication{
    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionApplication.class);

    private final UserRepository userRepository;

    public PermissionApplication(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public boolean canRead(Long applicationId, UserDetails userDetails) {
        ApplicationDto application = new ApplicationDto();
        application.setId(applicationId);
        return canRead(application, userDetails);
    }


    @Transactional(readOnly = true)
    public boolean canManage(Long applicationId, UserDetails userDetails) {
        ApplicationDto application = new ApplicationDto();
        application.setId(applicationId);
        return canManage(application, userDetails);
    }

    @Transactional(readOnly = true)
    public boolean canWrite(Long applicationId, UserDetails userDetails) {
        ApplicationDto application = new ApplicationDto();
        application.setId(applicationId);
        return canWrite(application, userDetails);
    }

    @Transactional(readOnly = true)
    public boolean canRead(ApplicationDto application, UserDetails userDetails) {
        LOGGER.debug("PermissionApplication canRead");
        User user = userRepository.findUserFromApplication(application.getId(), userDetails.getUsername());
        return user != null;
    }

    @Transactional(readOnly = true)
    public boolean canWrite(ApplicationDto application, UserDetails userDetails) {
        LOGGER.debug("PermissionApplication canWrite");
        User user = userRepository.findManagerFromApplication(application.getId(), userDetails.getUsername());
        return user != null;
    }

    @Transactional(readOnly = true)
    public boolean canManage(ApplicationDto application, UserDetails userDetails) {
        LOGGER.debug("PermissionApplication canManage");
        return userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Constants.ROLE_ADMIN));
    }
}
