package fr.icdc.ebad.service;

import fr.icdc.ebad.domain.GlobalSetting;
import fr.icdc.ebad.repository.GlobalSettingRepository;
import fr.icdc.ebad.service.util.EbadServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.cache.annotation.CacheRemoveAll;
import java.util.List;

@Service
public class GlobalSettingService {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalSettingService.class);
    private final GlobalSettingRepository globalSettingRepository;

    public GlobalSettingService(GlobalSettingRepository globalSettingRepository) {
        this.globalSettingRepository = globalSettingRepository;
    }

    @Transactional
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

    @CacheRemoveAll(cacheName = "global_settings")
    @Transactional
    public GlobalSetting setValue(String key, String value) throws EbadServiceException {
        GlobalSetting globalSetting = globalSettingRepository.findById(key).orElseThrow(EbadServiceException::new);
        globalSetting.setValue(value);
        return globalSettingRepository.save(globalSetting);
    }

    @Cacheable("global_settings")
    @Transactional(readOnly = true)
    public GlobalSetting getValue(String key) throws EbadServiceException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("get global setting {}", key);
        }
        return globalSettingRepository.findById(key).orElseThrow(EbadServiceException::new);
    }

    @Transactional(readOnly = true)
    public List<GlobalSetting> getAllGlobalSettings() {
        return globalSettingRepository.findAll();
    }
}
