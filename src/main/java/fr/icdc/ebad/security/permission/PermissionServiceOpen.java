package fr.icdc.ebad.security.permission;

import fr.icdc.ebad.domain.GlobalSetting;
import fr.icdc.ebad.service.GlobalSettingService;
import fr.icdc.ebad.service.util.EbadServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * Determine if a service is opened or closed
 */
@Service
public class PermissionServiceOpen {
    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionServiceOpen.class);
    private final GlobalSettingService globalSettingService;
    private final Environment env;

    public PermissionServiceOpen(GlobalSettingService globalSettingService, Environment env) {
        this.globalSettingService = globalSettingService;
        this.env = env;
    }

    public boolean canImportEnvironment() {
        return getServiceOpen("ENVIRONMENT_IMPORT_ENABLED");
    }

    public boolean canImportApplication() {
        return getServiceOpen("APPLICATION_IMPORT_ENABLED");
    }

    public boolean canCreateEnvironment() {
        return getServiceOpen("ENVIRONMENT_CREATE_ENABLED");
    }

    public boolean canCreateApplication() {
        return getServiceOpen("APPLICATION_CREATE_ENABLED");
    }

    public boolean canCreateOrUpdateUser() {
        return Arrays.stream(env.getActiveProfiles()).anyMatch("jwt"::equalsIgnoreCase);
    }

    private boolean getServiceOpen(String key) {
        try {
            GlobalSetting globalSetting = globalSettingService.getValue(key);
            return Boolean.parseBoolean(globalSetting.getValue());
        } catch (EbadServiceException e) {
            LOGGER.debug("Unable to get global settings {}", key, e);
            return false;
        }
    }
}
