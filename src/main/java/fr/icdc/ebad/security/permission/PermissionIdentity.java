package fr.icdc.ebad.security.permission;

import fr.icdc.ebad.config.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class PermissionIdentity {
    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionEnvironnement.class);

    private final PermissionApplication permissionApplication;

    public PermissionIdentity(PermissionApplication permissionApplication) {
        this.permissionApplication = permissionApplication;
    }


    public boolean canRead(Long applicationId, UserDetails userDetails) {
        LOGGER.debug("PermissionIdentity canRead");
        if (applicationId == null) {
            return userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Constants.ROLE_ADMIN)) ||
                    userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Constants.ROLE_USER));
        }
        return permissionApplication.canWrite(applicationId, userDetails);
    }

    public boolean canWrite(Long applicationId, UserDetails userDetails) {
        LOGGER.debug("PermissionIdentity canRead");
        if (applicationId == null) {
            return userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Constants.ROLE_ADMIN));
        }
        return permissionApplication.canWrite(applicationId, userDetails);
    }
}
