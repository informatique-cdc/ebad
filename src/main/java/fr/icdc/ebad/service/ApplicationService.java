package fr.icdc.ebad.service;

import com.querydsl.core.types.Predicate;
import fr.icdc.ebad.domain.Application;
import fr.icdc.ebad.domain.Environnement;
import fr.icdc.ebad.domain.UsageApplication;
import fr.icdc.ebad.domain.User;
import fr.icdc.ebad.plugin.plugin.EnvironnementConnectorPlugin;
import fr.icdc.ebad.repository.ApplicationRepository;
import fr.icdc.ebad.repository.TypeFichierRepository;
import fr.icdc.ebad.service.util.EbadServiceException;
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
public class ApplicationService {
    private static final String FIELD_NAME_APPLICATION = "name";
    private final ApplicationRepository applicationRepository;
    private final TypeFichierRepository typeFichierRepository;
    private final EnvironnementService environnementService;
    private final Optional<EnvironnementConnectorPlugin> optionalEnvironnementConnectorPlugin;

    public ApplicationService(ApplicationRepository applicationRepository, TypeFichierRepository typeFichierRepository, EnvironnementService environnementService, Optional<EnvironnementConnectorPlugin> optionalEnvironnementConnectorPlugin) {
        this.applicationRepository = applicationRepository;
        this.typeFichierRepository = typeFichierRepository;
        this.environnementService = environnementService;
        this.optionalEnvironnementConnectorPlugin = optionalEnvironnementConnectorPlugin;
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

    //FIXME DETERMINE NORME
    @Transactional
    public List<Environnement> importEnvironments(Long applicationId) throws EbadServiceException {
        Application application = this.getApplication(applicationId).orElseThrow(() -> new EbadServiceException("Aucune application trouvée"));
        EnvironnementConnectorPlugin environnementConnectorPlugin = optionalEnvironnementConnectorPlugin.orElseThrow(() -> new EbadServiceException("Aucun plugin correspondant trouvé"));
        return environnementConnectorPlugin.discoverFromApp(application.getCode()).stream().map(
                environnementDiscoverDto -> {
                    return Environnement.builder()
                            .application(application)
                            .host(environnementDiscoverDto.getHost())
                            .homePath(environnementDiscoverDto.getHost())
                            .login(environnementDiscoverDto.getLogin())
                            .name(environnementDiscoverDto.getName())
                            .prefix(environnementDiscoverDto.getPrefix())
                            .build();
                }).collect(Collectors.toList());
    }
}
