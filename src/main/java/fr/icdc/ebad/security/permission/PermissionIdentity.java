package fr.icdc.ebad.security.permission;

import fr.icdc.ebad.domain.Identity;
import fr.icdc.ebad.repository.IdentityRepository;
import fr.icdc.ebad.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PermissionIdentity {
    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionEnvironnement.class);

    private final PermissionApplication permissionApplication;
    private final IdentityRepository identityRepository;

    public PermissionIdentity(PermissionApplication permissionApplication, IdentityRepository identityRepository) {
        this.permissionApplication = permissionApplication;
        this.identityRepository = identityRepository;
    }


    public boolean canReadByApplication(Long applicationId, UserDetails userDetails) {
        LOGGER.debug("PermissionIdentity canReadByApplication");
        if (applicationId == null) {
            return SecurityUtils.isAdmin() ||  SecurityUtils.isUser();
        }
        return permissionApplication.canWrite(applicationId, userDetails);
    }

    public boolean canRead(Long identityId, UserDetails userDetails) {
        LOGGER.debug("PermissionIdentity canRead");
        if (identityId == null) {
            return false;
        }
        Optional<Identity> identityOptional = identityRepository.findById(identityId);
        if(identityOptional.isEmpty()){
            return false;
        }
        if(identityOptional.get().getAvailableApplication() == null){
            return SecurityUtils.isAdmin() ||  SecurityUtils.isUser();
        }
        return canReadByApplication(identityOptional.get().getAvailableApplication().getId(), userDetails);
    }

    public boolean canWriteByApplication(Long applicationId, UserDetails userDetails) {
        LOGGER.debug("PermissionIdentity canWriteByApplication");
        if (applicationId == null) {
            return SecurityUtils.isAdmin();
        }
        return permissionApplication.canWrite(applicationId, userDetails);
    }

    public boolean canWrite(Long identityId, UserDetails userDetails) {
        LOGGER.debug("PermissionIdentity canWrite");
        if (identityId == null) {
            return false;
        }
        Optional<Identity> identityOptional = identityRepository.findById(identityId);
        if(identityOptional.isEmpty()){
            return false;
        }
        if(identityOptional.get().getAvailableApplication() == null){
            return SecurityUtils.isAdmin();
        }
        return canWriteByApplication(identityOptional.get().getAvailableApplication().getId(), userDetails);
    }
}
