package fr.icdc.ebad.security;

import fr.icdc.ebad.domain.GlobalSetting;
import fr.icdc.ebad.service.GlobalSettingService;
import fr.icdc.ebad.service.util.EbadServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Determine if a service is opened or closed
 */
@Service
public class PermissionServiceOpen {
    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionServiceOpen.class);
    private final GlobalSettingService globalSettingService;

    public PermissionServiceOpen(GlobalSettingService globalSettingService) {
        this.globalSettingService = globalSettingService;
    }

    public boolean canImportEnvironment() {
        try {
            GlobalSetting globalSetting = globalSettingService.getValue("ENVIRONMENT_IMPORT_ENABLED");
            return Boolean.parseBoolean(globalSetting.getValue());
        } catch (EbadServiceException e) {
            LOGGER.debug("Unable to get global settings ENVIRONMENT_IMPORT_ENABLED", e);
            return false;
        }
    }

    public boolean canImportApplication() {
        try {
            GlobalSetting globalSetting = globalSettingService.getValue("APPLICATION_IMPORT_ENABLED");
            return Boolean.parseBoolean(globalSetting.getValue());
        } catch (EbadServiceException e) {
            LOGGER.debug("Unable to get global settings APPLICATION_IMPORT_ENABLED", e);
            return false;
        }
    }
}
