package fr.icdc.ebad.service;

import com.querydsl.core.types.Predicate;
import fr.icdc.ebad.domain.Application;
import fr.icdc.ebad.domain.UsageApplication;
import fr.icdc.ebad.domain.User;
import fr.icdc.ebad.plugin.dto.ApplicationDiscoverDto;
import fr.icdc.ebad.plugin.plugin.ApplicationConnectorPlugin;
import fr.icdc.ebad.repository.AccreditationRequestRepository;
import fr.icdc.ebad.repository.ApplicationRepository;
import fr.icdc.ebad.repository.TypeFichierRepository;
import fr.icdc.ebad.repository.UsageApplicationRepository;
import fr.icdc.ebad.service.util.EbadServiceException;
import org.pf4j.PluginRuntimeException;
import org.pf4j.PluginWrapper;
import org.pf4j.spring.SpringPluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;


@Service
@DependsOn("springPluginManager")
public class ApplicationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationService.class);
    private final ApplicationRepository applicationRepository;
    private final TypeFichierRepository typeFichierRepository;
    private final UsageApplicationRepository usageApplicationRepository;
    private final EnvironnementService environnementService;
    private final List<ApplicationConnectorPlugin> applicationConnectorPlugins;
    private final SpringPluginManager springPluginManager;
    private final AccreditationRequestRepository accreditationRequestRepository;

    public ApplicationService(ApplicationRepository applicationRepository, TypeFichierRepository typeFichierRepository, UsageApplicationRepository usageApplicationRepository, EnvironnementService environnementService, List<ApplicationConnectorPlugin> applicationConnectorPlugins, SpringPluginManager springPluginManager, AccreditationRequestRepository accreditationRequestRepository) {
        this.applicationRepository = applicationRepository;
        this.typeFichierRepository = typeFichierRepository;
        this.usageApplicationRepository = usageApplicationRepository;
        this.environnementService = environnementService;
        this.applicationConnectorPlugins = applicationConnectorPlugins;
        this.springPluginManager = springPluginManager;
        this.accreditationRequestRepository = accreditationRequestRepository;
    }


    @Transactional(readOnly = true)
    public Page<Application> getAllApplications(Predicate predicate, Pageable pageable) {
        return applicationRepository.findAll(predicate, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Application> findApplication(Predicate predicate, Pageable pageable) {
        return applicationRepository.findAll(predicate, pageable);
    }

    @Transactional(readOnly = true)
    public List<Application> findApplication(Predicate predicate) {
        return findApplication(predicate, PageRequest.of(0, Integer.MAX_VALUE)).getContent();
    }

    @Transactional(readOnly = true)
    public Optional<Application> getApplication(Long id) {
        return applicationRepository.findById(id);
    }


    @Transactional
    public void deleteApplication(Long appId) {
        Application application = applicationRepository.getOne(appId);
        accreditationRequestRepository.deleteByApplication(application);
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
        return getUsersOrManager(applicationId, false, true);
    }

    @Transactional
    public Set<User> getUsers(Long applicationId) {
        return getUsersOrManager(applicationId, true, false);
    }

    @Transactional
    public Set<User> getUsersOrManager(Long applicationId, boolean canUser, boolean canManage) {
        Set<UsageApplication> usageApplications = getApplication(applicationId).orElseGet(Application::new).getUsageApplications();
        Set<User> users = new HashSet<>();
        for (UsageApplication usageApplication : usageApplications) {
            if ((usageApplication.isCanUse() && canUser) || (usageApplication.isCanManage() && canManage)) {
                users.add(usageApplication.getUser());
            }
        }
        return users;
    }


    @Transactional
    @PreAuthorize("@permissionServiceOpen.canImportApplication()")
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
                    application.setExternalId(applicationDiscoverDto.getId());
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
            } catch (PluginRuntimeException e) {
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

    @Transactional(readOnly = true)
    public Page<Application> getAllApplicationsManaged(Pageable pageable, String username) {
        return applicationRepository.findAllManagedByUser(username, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Application> getAllApplicationsUsed(Pageable pageable, String username) {
        return applicationRepository.findAllUsagedByUser(username, pageable);
    }

    @Transactional(readOnly = true)
    public Page<UsageApplication> getUsage(Pageable pageable, Long appId) {
        return usageApplicationRepository.findAllByApplicationId(appId, pageable);
    }
}
