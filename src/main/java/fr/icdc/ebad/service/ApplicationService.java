package fr.icdc.ebad.service;

import com.querydsl.core.types.Predicate;
import fr.icdc.ebad.domain.Application;
import fr.icdc.ebad.domain.Environnement;
import fr.icdc.ebad.domain.UsageApplication;
import fr.icdc.ebad.domain.User;
import fr.icdc.ebad.plugin.dto.NormeDiscoverDto;
import fr.icdc.ebad.plugin.plugin.EnvironnementConnectorPlugin;
import fr.icdc.ebad.repository.ApplicationRepository;
import fr.icdc.ebad.repository.EnvironnementRepository;
import fr.icdc.ebad.repository.NormeRepository;
import fr.icdc.ebad.repository.TypeFichierRepository;
import fr.icdc.ebad.service.util.EbadServiceException;
import ma.glasnost.orika.MapperFacade;
import org.pf4j.PluginException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
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
    private final List<EnvironnementConnectorPlugin> environnementConnectorPluginList;
    private final MapperFacade mapper;
    private final NormeRepository normeRepository;
    private final EnvironnementRepository environnementRepository;

    public ApplicationService(ApplicationRepository applicationRepository, TypeFichierRepository typeFichierRepository, EnvironnementService environnementService, List<EnvironnementConnectorPlugin> environnementConnectorPluginList, MapperFacade mapper, NormeRepository normeRepository, EnvironnementRepository environnementRepository) {
        this.applicationRepository = applicationRepository;
        this.typeFichierRepository = typeFichierRepository;
        this.environnementService = environnementService;
        this.environnementConnectorPluginList = environnementConnectorPluginList;
        this.mapper = mapper;
        this.normeRepository = normeRepository;
        this.environnementRepository = environnementRepository;
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
        try {
            Set<Environnement> environnementsImported = importEnvironments(applicationResult.getId());
            environnementRepository.saveAll(environnementsImported);
        } catch (EbadServiceException e) {
            LOGGER.error("Impossible d'importer les environnements", e);
        }
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
    public Set<Environnement> importEnvironments(Long applicationId) throws EbadServiceException {
        Application application = this.getApplication(applicationId).orElseThrow(() -> new EbadServiceException("Aucune application trouvée"));
        Set<Environnement> environnements = new HashSet<>();
        List<NormeDiscoverDto> normeDiscoverDtos = mapper.mapAsList(normeRepository.findAll(), NormeDiscoverDto.class);

        try {
            for (EnvironnementConnectorPlugin environnementConnectorPlugin : environnementConnectorPluginList) {
                List<Environnement> environnementList = mapper.mapAsList(environnementConnectorPlugin.discoverFromApp(application.getCode(), normeDiscoverDtos), Environnement.class);
                for (Environnement environnement : environnementList) {
                    environnement.setApplication(application);
                }
                environnements.addAll(environnementList);
            }
        } catch (PluginException e) {
            LOGGER.error("Une erreur est survenue lors de l'import des environnements : {}", e.getMessage(), e);
            environnements = new HashSet<>();
        }
        return environnements;
    }
}
