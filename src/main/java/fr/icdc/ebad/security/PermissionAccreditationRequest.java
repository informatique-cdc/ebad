package fr.icdc.ebad.security;

import fr.icdc.ebad.domain.AccreditationRequest;
import fr.icdc.ebad.repository.AccreditationRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Determine if a service is opened or closed
 */
@Service
public class PermissionAccreditationRequest {
    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionAccreditationRequest.class);
    private final AccreditationRequestRepository accreditationRequestRepository;
    private final PermissionApplication permissionApplication;

    public PermissionAccreditationRequest(AccreditationRequestRepository accreditationRequestRepository, PermissionApplication permissionApplication) {
        this.accreditationRequestRepository = accreditationRequestRepository;
        this.permissionApplication = permissionApplication;
    }


    public boolean canAcceptAccreditationRequest(Long id, UserDetails userDetails) {
        if (SecurityUtils.isAdmin()) {
            return true;
        }
        Optional<AccreditationRequest> accreditationRequest = accreditationRequestRepository.findById(id);
        return accreditationRequest.filter(request -> permissionApplication.canWrite(request.getApplication().getId(), userDetails)).isPresent();
    }
}
