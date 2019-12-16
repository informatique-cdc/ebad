package fr.icdc.ebad.service;

import com.querydsl.core.types.Predicate;
import fr.icdc.ebad.domain.Application;
import fr.icdc.ebad.domain.UsageApplication;
import fr.icdc.ebad.domain.User;
import fr.icdc.ebad.plugin.dto.ApplicationDiscoverDto;
import fr.icdc.ebad.plugin.plugin.ApplicationConnectorPlugin;
import fr.icdc.ebad.repository.ApplicationRepository;
import fr.icdc.ebad.repository.TypeFichierRepository;
import fr.icdc.ebad.service.util.EbadServiceException;
import org.pf4j.PluginException;
import org.pf4j.PluginWrapper;
import org.pf4j.spring.SpringPluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;


@Service
@DependsOn("springPluginManager")
public class ApplicationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationService.class);
    private static final String FIELD_NAME_APPLICATION = "name";
    private final ApplicationRepository applicationRepository;
    private final TypeFichierRepository typeFichierRepository;
    private final EnvironnementService environnementService;
    private final List<ApplicationConnectorPlugin> applicationConnectorPlugins;
    private final SpringPluginManager springPluginManager;

    public ApplicationService(ApplicationRepository applicationRepository, TypeFichierRepository typeFichierRepository, EnvironnementService environnementService, List<ApplicationConnectorPlugin> applicationConnectorPlugins, SpringPluginManager springPluginManager) {
        this.applicationRepository = applicationRepository;
        this.typeFichierRepository = typeFichierRepository;
        this.environnementService = environnementService;
        this.applicationConnectorPlugins = applicationConnectorPlugins;
        this.springPluginManager = springPluginManager;
    }


    @Transactional(readOnly = true)
    public List<Application> getAllApplications() {
        return applicationRepository.findAll(new Sort(Sort.Direction.DESC, FIELD_NAME_APPLICATION));
    }

    @Transactional(readOnly = true)
    public List<Application> findApplication(Predicate predicate) {
        return StreamSupport
                .stream(applicationRepository.findAll(predicate).spliterator(), false)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<Application> getApplication(Long id) {
        return applicationRepository.findById(id);
    }


    @Transactional
    public void deleteApplication(Long appId) {
        Application application = applicationRepository.getOne(appId);
        typeFichierRepository.deleteByApplication(application);
        application.getEnvironnements().forEach(environnement -> environnementService.deleteEnvironnement(environnement, true));
        applicationRepository.delete(application);
    }

    @Transactional
    public Application saveApplication(Application application) {
        Application applicationResult = applicationRepository.save(application);
        applicationResult.getEnvironnements();
        return application;
    }

    @Transactional
    public Set<User> getManagers(Long applicationId) {
        Set<UsageApplication> usageApplications = getApplication(applicationId).orElseGet(Application::new).getUsageApplications();
        Set<User> users = new HashSet<>();
        for (UsageApplication usageApplication : usageApplications) {
            if (usageApplication.isCanManage()) {
                users.add(usageApplication.getUser());
            }
        }
        return users;
    }

    @Transactional
    public Set<User> getUsers(Long applicationId) {
        Set<UsageApplication> usageApplications = getApplication(applicationId).orElseGet(Application::new).getUsageApplications();
        Set<User> users = new HashSet<>();
        for (UsageApplication usageApplication : usageApplications) {
            if (usageApplication.isCanUse()) {
                users.add(usageApplication.getUser());
            }
        }
        return users;
    }


    @Transactional
    public String importApp() {
        StringBuilder result = new StringBuilder();
        for (ApplicationConnectorPlugin applicationConnectorPlugin : applicationConnectorPlugins) {
            PluginWrapper pluginWrapper = springPluginManager.whichPlugin(applicationConnectorPlugin.getClass());
            String pluginId = pluginWrapper.getPluginId();
            LOGGER.info("PluginWrapper = {}", pluginId);
            try {
                List<ApplicationDiscoverDto> applicationDiscoverDtoList = applicationConnectorPlugin.discoverApp();
                for (ApplicationDiscoverDto applicationDiscoverDto : applicationDiscoverDtoList) {
                    if (applicationDiscoverDto.getCode().length() != 3) {
                        continue;
                    }
                    Application application = applicationRepository
                            .findAllByExternalIdAndPluginId(applicationDiscoverDto.getId(), pluginId)
                            .orElse(new Application());
                    application.setName(applicationDiscoverDto.getName());
                    application.setCode(applicationDiscoverDto.getCode());
                    application.setDateParametrePattern("yyyyMMdd");
                    application.setDateFichierPattern("yyyyMMdd");
                    application.setExternalId(applicationDiscoverDto.getId().toString());
                    application.setPluginId(pluginId);
                    try {
                        applicationRepository.save(application);
                        LOGGER.debug("application imported {}", applicationDiscoverDto);
                        result.append("application imported ").append(applicationDiscoverDto).append("\n");
                    } catch (DataIntegrityViolationException e) {
                        LOGGER.debug("error when try to import application {}", applicationDiscoverDto);
                        result.append("error when try to import application ").append(applicationDiscoverDto).append("\n");
                    }
                }
            } catch (PluginException e) {
                LOGGER.error("error when import applications", e);
                result.append("error when import applications with plugin ").append(pluginId).append("\n");
            }
        }
        return result.toString();
    }

    @Transactional
    public Application updateApplication(Application application) throws EbadServiceException {
        Application oldApplication = getApplication(application.getId()).orElseThrow(EbadServiceException::new);
        Set<UsageApplication> usageApplications = oldApplication.getUsageApplications();
        application.setUsageApplications(usageApplications);
        application.setExternalId(oldApplication.getExternalId());
        application.setPluginId(oldApplication.getPluginId());
        return saveApplication(application);
    }
}
