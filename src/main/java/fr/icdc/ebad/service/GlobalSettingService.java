package fr.icdc.ebad.service;

import fr.icdc.ebad.domain.GlobalSetting;
import fr.icdc.ebad.repository.GlobalSettingRepository;
import fr.icdc.ebad.service.util.EbadServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class GlobalSettingService {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalSettingService.class);
    private final GlobalSettingRepository globalSettingRepository;

    public GlobalSettingService(GlobalSettingRepository globalSettingRepository) {
        this.globalSettingRepository = globalSettingRepository;
    }

    public GlobalSetting getGlobalSetting(String key) throws EbadServiceException {
        return globalSettingRepository.findById(key).orElseThrow(EbadServiceException::new);
    }

    public GlobalSetting saveGlobalSetting(String key, String value, String description, String label) throws EbadServiceException {
        if (globalSettingRepository.findById(key).isPresent()) {
            throw new EbadServiceException("Global Setting " + key + " already exist");
        }
        GlobalSetting globalSetting = GlobalSetting
                .builder()
                .key(key)
                .value(value)
                .description(description)
                .label(label)
                .build();

        return globalSettingRepository.save(globalSetting);
    }

    @CachePut("global_settings")
    public GlobalSetting setValue(String key, String value) throws EbadServiceException {
        GlobalSetting globalSetting = globalSettingRepository.findById(key).orElseThrow(EbadServiceException::new);
        globalSetting.setValue(value);
        return globalSettingRepository.save(globalSetting);
    }

    @Cacheable("global_settings")
    public String getValue(String key) throws EbadServiceException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("get global setting {}", key);
        }
        GlobalSetting globalSetting = globalSettingRepository.findById(key).orElseThrow(EbadServiceException::new);
        return globalSetting.getValue();
    }
}
