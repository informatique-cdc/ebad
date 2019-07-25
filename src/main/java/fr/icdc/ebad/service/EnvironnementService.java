package fr.icdc.ebad.service;

import com.jcraft.jsch.JSchException;
import fr.icdc.ebad.domain.Environnement;
import fr.icdc.ebad.domain.util.RetourBatch;
import fr.icdc.ebad.repository.BatchRepository;
import fr.icdc.ebad.repository.ChaineRepository;
import fr.icdc.ebad.repository.DirectoryRepository;
import fr.icdc.ebad.repository.EnvironnementRepository;
import fr.icdc.ebad.repository.LogBatchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

/**
 * Created by dtrouillet on 03/03/2016.
 */
@Service
public class EnvironnementService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnvironnementService.class);

    private static final String SANS_REPONSE = "";
    public static final int CODE_SUCCESS = 0;
    public static final String FR_DATE_FORMAT = "ddMMyyyy";
    public static final String US_DATE_FORMAT = "yyyyMMdd";

    private final ShellService shellService;
    private final EnvironnementRepository environnementRepository;
    private final BatchRepository batchRepository;
    private final LogBatchRepository logBatchRepository;
    private final ChaineRepository chaineRepository;
    private final DirectoryRepository directoryRepository;

    public EnvironnementService(ShellService shellService, EnvironnementRepository environnementRepository, BatchRepository batchRepository, LogBatchRepository logBatchRepository, ChaineRepository chaineRepository, DirectoryRepository directoryRepository) {
        this.shellService = shellService;
        this.environnementRepository = environnementRepository;
        this.batchRepository = batchRepository;
        this.logBatchRepository = logBatchRepository;
        this.chaineRepository = chaineRepository;
        this.directoryRepository = directoryRepository;
    }


    @Nullable
    @Transactional(readOnly = true)
    public Date getDateTraiement(Long id) {
        Environnement environnement = getEnvironnement(id);
        try {
            RetourBatch retourBatch = shellService.runCommand(environnement, "cat " + environnement.getHomePath() + "/" + environnement.getNorme().getCtrlMDate());
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
        } catch (JSchException | IOException | ParseException e) {
            LOGGER.warn("Erreur lors de la récupération de la date traitement", e);
            return null;
        }
    }

    @Transactional(readOnly = true)
    public boolean changeDateTraiement(Long environnementId, Date dateTraitement) {
        Environnement environnement = getEnvironnement(environnementId);
        try {
            SimpleDateFormat dateFormatCtrlM = new SimpleDateFormat(Optional.ofNullable(environnement.getApplication().getDateFichierPattern()).orElse(FR_DATE_FORMAT));
            String date = dateFormatCtrlM.format(dateTraitement);
            RetourBatch retourBatch = shellService.runCommand(environnement, "echo " + date + " > " + environnement.getHomePath() + "/" + environnement.getNorme().getCtrlMDate());
            return retourBatch.getReturnCode() == CODE_SUCCESS;
        } catch (JSchException | IOException e) {
            LOGGER.warn("Erreur lors de la modification de la date traitement", e);
            return false;
        }
    }

    //FIXME Mise en place norme
    @NotNull
    @Transactional(readOnly = true)
    public String getEspaceDisque(Long id) {
        Environnement environnement = getEnvironnement(id);
        try {
            RetourBatch retourBatch = shellService.runCommand(environnement, "echo $( df -m " + environnement.getHomePath() + " | tail -1 | awk ' { print $4 } ' )");
            if (retourBatch.getReturnCode() == CODE_SUCCESS) {
                LOGGER.debug("Espace disque = {}", retourBatch.getLogOut());
                return retourBatch.getLogOut().replace("%", "");
            }
            return SANS_REPONSE;
        } catch (JSchException | IOException e) {
            LOGGER.warn("Erreur lors de la récupération de l'espace disque", e);
            return SANS_REPONSE;
        }
    }

    //FIXME Mise en place norme
    @Transactional(readOnly = true)
    public void purgerLogs(Long id) {
        Environnement environnement = getEnvironnement(id);

        try {
            RetourBatch retourBatch = shellService.runCommand(environnement, "find " + environnement.getHomePath() + "/logctm -type f -exec rm -v  {} ';'");
            if (retourBatch.getReturnCode() == CODE_SUCCESS) {
                LOGGER.debug("Purge logctm OK {}", retourBatch.getLogOut());
                return;
            }
            LOGGER.warn("Purge logctm KO {}", retourBatch.getLogOut());
        } catch (JSchException | IOException e) {
            LOGGER.warn("Erreur lors de la purge logctm", e);
        }
    }

    //FIXME Mise en place norme
    @Transactional
    public void purgerArchive(Long id) {
        Environnement environnement = getEnvironnement(id);
        try {
            RetourBatch retourBatch = shellService.runCommand(environnement, "find " + environnement.getHomePath() + "/archive -type f -exec rm -v  {} ';'");
            if (retourBatch.getReturnCode() == CODE_SUCCESS) {
                LOGGER.debug("Purge archive OK {}", retourBatch.getLogOut());
                return;
            }
            LOGGER.warn("Purge archive KO {}", retourBatch.getLogOut());
        } catch (JSchException | IOException e) {
            LOGGER.warn("Erreur lors de la purge archive", e);
        }
    }

    @Transactional
    public void deleteEnvironnement(Environnement environnement, boolean withBatchs) {
        logBatchRepository.deleteByEnvironnement(environnement);
        chaineRepository.deleteByEnvironnement(environnement);
        if (withBatchs) {
            batchRepository.deleteAll(environnement.getBatchs());
        }

        directoryRepository.deleteByEnvironnement(environnement);
        environnementRepository.delete(environnement);
    }

    @Transactional(readOnly = true)
    public Environnement getEnvironnement(Long environnementId) {
        return environnementRepository.getOne(environnementId);
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
}
