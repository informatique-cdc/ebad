package fr.icdc.ebad.service;

import com.querydsl.core.types.Predicate;
import fr.icdc.ebad.domain.*;
import fr.icdc.ebad.domain.util.RetourBatch;
import fr.icdc.ebad.plugin.dto.EnvironnementDiscoverDto;
import fr.icdc.ebad.plugin.dto.NormeDiscoverDto;
import fr.icdc.ebad.plugin.plugin.EnvironnementConnectorPlugin;
import fr.icdc.ebad.repository.*;
import fr.icdc.ebad.service.util.EbadServiceException;
import ma.glasnost.orika.MapperFacade;
import org.jobrunr.scheduling.JobScheduler;
import org.pf4j.PluginRuntimeException;
import org.pf4j.PluginWrapper;
import org.pf4j.spring.SpringPluginManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static fr.icdc.ebad.config.Constants.GLOBAL_SETTINGS_DEFAULT_IDENTITY_ID;

/**
 * Created by dtrouillet on 03/03/2016.
 */
@Service
public class EnvironnementService {

    public static final int CODE_SUCCESS = 0;
    public static final String FR_DATE_FORMAT = "ddMMyyyy";
    public static final String US_DATE_FORMAT = "yyyyMMdd";
    private static final Logger LOGGER = LoggerFactory.getLogger(EnvironnementService.class);
    private static final String SANS_REPONSE = "";
    private final ShellService shellService;
    private final EnvironnementRepository environnementRepository;
    private final BatchRepository batchRepository;
    private final LogBatchRepository logBatchRepository;
    private final ChaineRepository chaineRepository;
    private final DirectoryRepository directoryRepository;
    private final NormeRepository normeRepository;
    private final MapperFacade mapper;
    private final List<EnvironnementConnectorPlugin> environnementConnectorPluginList;
    private final ApplicationRepository applicationRepository;
    private final SpringPluginManager springPluginManager;
    private final SchedulingRepository schedulingRepository;
    private final JobScheduler jobScheduler;
    private final GlobalSettingService globalSettingService;
    private final IdentityRepository identityRepository;

    public EnvironnementService(ShellService shellService, EnvironnementRepository environnementRepository, BatchRepository batchRepository, LogBatchRepository logBatchRepository, ChaineRepository chaineRepository, DirectoryRepository directoryRepository, NormeRepository normeRepository, MapperFacade mapper, List<EnvironnementConnectorPlugin> environnementConnectorPluginList, ApplicationRepository applicationRepository, SpringPluginManager springPluginManager, SchedulingRepository schedulingRepository, JobScheduler jobScheduler, GlobalSettingService globalSettingService, IdentityRepository identityRepository) {
        this.shellService = shellService;
        this.environnementRepository = environnementRepository;
        this.batchRepository = batchRepository;
        this.logBatchRepository = logBatchRepository;
        this.chaineRepository = chaineRepository;
        this.directoryRepository = directoryRepository;
        this.normeRepository = normeRepository;
        this.mapper = mapper;
        this.environnementConnectorPluginList = environnementConnectorPluginList;
        this.applicationRepository = applicationRepository;
        this.springPluginManager = springPluginManager;
        this.schedulingRepository = schedulingRepository;
        this.jobScheduler = jobScheduler;
        this.globalSettingService = globalSettingService;
        this.identityRepository = identityRepository;
    }


    @Nullable
    @Transactional(readOnly = true)
    public Date getDateTraiement(Long id) {
        Environnement environnement = getEnvironnement(id);
        try {
            RetourBatch retourBatch = shellService.runCommandNew(environnement, "cat " + environnement.getHomePath() + "/" + environnement.getNorme().getCtrlMDate());
            if (retourBatch.getReturnCode() == CODE_SUCCESS) {
                Date dateTraitement;
                String dateStr = retourBatch.getLogOut();
                SimpleDateFormat dateFormatCtrlM;
                try {
                    dateFormatCtrlM = new SimpleDateFormat(environnement.getApplication().getDateFichierPattern());
                    dateTraitement = dateFormatCtrlM.parse(dateStr);
                } catch (NullPointerException | ParseException parseException) {
                    dateFormatCtrlM = new SimpleDateFormat(US_DATE_FORMAT);
                    dateTraitement = dateFormatCtrlM.parse(dateStr);
                }
                SimpleDateFormat dateFr = new SimpleDateFormat(FR_DATE_FORMAT);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Date Traitement = {}", dateFr.format(dateTraitement));
                }
                return dateTraitement;
            }
            return null;
        } catch (EbadServiceException | ParseException e) {
            LOGGER.warn("Erreur lors de la récupération de la date traitement", e);
            return null;
        }
    }

    @Transactional(readOnly = true)
    public void changeDateTraiement(Long environnementId, Date dateTraitement) throws EbadServiceException {
        Environnement environnement = getEnvironnement(environnementId);
        SimpleDateFormat dateFormatCtrlM = new SimpleDateFormat(Optional.ofNullable(environnement.getApplication().getDateFichierPattern()).orElse(FR_DATE_FORMAT));
        String date = dateFormatCtrlM.format(dateTraitement);
        shellService.runCommandNew(environnement, "echo " + date + " > " + environnement.getHomePath() + "/" + environnement.getNorme().getCtrlMDate());
    }

    //FIXME Mise en place norme
    @NotNull
    @Transactional(readOnly = true)
    public String getEspaceDisque(Long id) {
        Environnement environnement = getEnvironnement(id);
        try {
            RetourBatch retourBatch = shellService.runCommandNew(environnement, "echo $( df -m " + environnement.getHomePath() + " | tail -1 | awk ' { print $4 } ' )");
            if (retourBatch.getReturnCode() == CODE_SUCCESS) {
                LOGGER.debug("Espace disque = {}", retourBatch.getLogOut());
                return retourBatch.getLogOut().replace("%", "");
            }
            return SANS_REPONSE;
        } catch (EbadServiceException e) {
            LOGGER.warn("Erreur lors de la récupération de l'espace disque", e);
            return SANS_REPONSE;
        }
    }

    @Transactional
    public void deleteScheduledJobFromEnvironment(Long environmentId){
        List<Scheduling> schedulings = schedulingRepository.findAllByEnvironnementId(environmentId);
        schedulings.forEach(scheduling -> {
            jobScheduler.delete(String.valueOf(scheduling.getId()));
            schedulingRepository.delete(scheduling);
        });
    }

    @Transactional
    public void deleteEnvironnement(Environnement environnement, boolean withBatchs) {
        logBatchRepository.deleteByEnvironnement(environnement);
        chaineRepository.deleteByEnvironnement(environnement);
        deleteScheduledJobFromEnvironment(environnement.getId());
        if (withBatchs) {
            batchRepository.deleteAll(environnement.getBatchs());
        }

        directoryRepository.deleteByEnvironnement(environnement);
        environnementRepository.delete(environnement);
    }

    @Transactional(readOnly = true)
    public Environnement getEnvironnement(Long environnementId) {
        return environnementRepository.getById(environnementId);
    }

    @Transactional
    public Environnement saveEnvironnement(Environnement environnement) {
        return environnementRepository.saveAndFlush(environnement);
    }

    @Transactional(readOnly = true)
    public Optional<Environnement> findEnvironnement(Long environnementId) {
        return environnementRepository.findById(environnementId);
    }

    @Transactional
    public Environnement updateEnvironnement(Environnement env) {
        Environnement oldEnv = getEnvironnement(env.getId());
        env.setBatchs(oldEnv.getBatchs());
        env.setLogBatchs(oldEnv.getLogBatchs());
        env.setApplication(oldEnv.getApplication());
        return saveEnvironnement(env);
    }

    @Transactional
    @PreAuthorize("@permissionServiceOpen.canImportEnvironment()")
    public Set<Environnement> importEnvironments(Long applicationId) throws EbadServiceException {
        Application application = applicationRepository.findById(applicationId).orElseThrow(() -> new EbadServiceException("No application with id " + applicationId));
        Set<Environnement> environnements = new HashSet<>();
        List<NormeDiscoverDto> normeDiscoverDtos = mapper.mapAsList(normeRepository.findAll(), NormeDiscoverDto.class);
        GlobalSetting defaultIdentityId = globalSettingService.getValue(GLOBAL_SETTINGS_DEFAULT_IDENTITY_ID);
        Identity identity = identityRepository.getById(Long.valueOf(defaultIdentityId.getValue()));

        try {
            for (EnvironnementConnectorPlugin environnementConnectorPlugin : environnementConnectorPluginList) {
                PluginWrapper pluginWrapper = springPluginManager.whichPlugin(environnementConnectorPlugin.getClass());
                String pluginId = pluginWrapper.getPluginId();
                List<EnvironnementDiscoverDto> environnementDiscoverDtos = environnementConnectorPlugin.discoverFromApp(application.getCode(), application.getName(), normeDiscoverDtos);
                for (EnvironnementDiscoverDto environnementDiscoverDto : environnementDiscoverDtos) {
                    Environnement environnement = environnementRepository
                            .findAllByExternalIdAndPluginId(environnementDiscoverDto.getId(), pluginId)
                            .orElse(new Environnement());

                    environnement.setName(environnementDiscoverDto.getName());
                    environnement.setHost(environnementDiscoverDto.getHost());
                    environnement.setHomePath(environnementDiscoverDto.getHome());
                    environnement.setPrefix(environnementDiscoverDto.getPrefix());
                    environnement.setNorme(mapper.map(environnementDiscoverDto.getNorme(), Norme.class));
                    environnement.setExternalId(environnementDiscoverDto.getId());
                    environnement.setPluginId(pluginId);
                    environnement.setApplication(application);
                    environnement.setIdentity(identity);

                    try {
                        environnementRepository.save(environnement);
                        environnements.add(environnement);
                        LOGGER.debug("Environment imported {}", environnementDiscoverDto);
                    } catch (DataIntegrityViolationException e) {
                        LOGGER.error("error when try to import environment {}", environnementDiscoverDto, e);
                    }
                }
            }
        } catch (PluginRuntimeException e) {
            LOGGER.error("Une erreur est survenue lors de l'import des environnements : {}", e.getMessage(), e);
            environnements = new HashSet<>();
        }
        return environnements;
    }

    @Transactional
    @PreAuthorize("@permissionServiceOpen.canImportEnvironment()")
    public List<Environnement> importEnvironments() throws EbadServiceException {
        List<Application> applicationList = applicationRepository.findAll();
        List<Environnement> environnementList = new ArrayList<>();
        for (Application application : applicationList) {
            Set<Environnement> environnements = importEnvironments(application.getId());
            application.setEnvironnements(environnements);
            applicationRepository.save(application);
            environnementList.addAll(environnements);
        }
        return environnementList;
    }

    public Page<Environnement> getEnvironmentFromApp(Long appId, Predicate predicate, Pageable pageable) {
        Predicate predicateAll = QEnvironnement.environnement.application.id.eq(appId).and(predicate);
        return environnementRepository.findAll(predicateAll, pageable);
    }
}
