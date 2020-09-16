package fr.icdc.ebad.service;

import com.jcraft.jsch.JSchException;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import fr.icdc.ebad.domain.*;
import fr.icdc.ebad.domain.util.RetourBatch;
import fr.icdc.ebad.repository.BatchRepository;
import fr.icdc.ebad.repository.LogBatchRepository;
import fr.icdc.ebad.security.SecurityUtils;
import fr.icdc.ebad.service.util.EbadServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class BatchService {
    /**
     * Execution des batchs
     * plus d'infos Jcraft sur http://www.jcraft.com/jsch/examples/
     */

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchService.class);
    public static final int INTERVAL_CLEAN_BATCH = 3600000;
    public static final String DEFAULT_DATE_FORMAT = "ddMMyyyy";

    private final BatchRepository batchRepository;
    private final ShellService shellService;
    private final EnvironnementService environnementService;
    private final UserService userService;
    private final LogBatchRepository logBatchRepository;
    private final NotificationService notificationService;
    private final NormeService normeService;

    public BatchService(LogBatchRepository logBatchRepository, ShellService shellService, BatchRepository batchRepository, EnvironnementService environnementService, UserService userService, NotificationService notificationService, NormeService normeService) {
        this.logBatchRepository = logBatchRepository;
        this.shellService = shellService;
        this.batchRepository = batchRepository;
        this.environnementService = environnementService;
        this.userService = userService;
        this.notificationService = notificationService;
        this.normeService = normeService;
    }


    @Transactional
    public RetourBatch runBatch(Long batchId, Long environnementId, String params) throws JSchException, IOException, EbadServiceException {
        Batch batch = batchRepository.getOne(batchId);
        batch.setParams(params);
        Environnement environnement = environnementService.getEnvironnement(environnementId);
        return runBatch(batch, environnement);
    }

    @Transactional
    public RetourBatch runBatch(Batch batch, Environnement environnement) throws JSchException, IOException, EbadServiceException {

        String params = batch.getDefaultParam();
        if (null != batch.getParams()) {
            params = batch.getParams();
        }
        Date dateTraitement = environnementService.getDateTraiement(environnement.getId());
        String[] paramsArray = params.split(" ");
        StringBuilder realParams = new StringBuilder();
        for (String param : paramsArray) {
            if ("${DATE_TRAITEMENT}".equals(param)) {

                String dateParam;
                SimpleDateFormat dateFormat;
                if (!StringUtils.isEmpty(environnement.getApplication().getDateParametrePattern())) {
                    dateFormat = new SimpleDateFormat(environnement.getApplication().getDateParametrePattern());
                } else {
                    dateFormat = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
                }
                if (null != dateTraitement) {
                    dateParam = dateFormat.format(dateTraitement);
                    realParams.append(dateParam).append(" ");
                }
            } else {
                realParams.append(param).append(" ");
            }
        }


        String command = environnement.getHomePath() + "/" + normeService.getShellPath(environnement.getNorme(), environnement.getApplication().getCode()) + environnement.getPrefix() + batch.getPath() + " " + realParams;


        LOGGER.debug("Execute batch with command : '{}'", command);

        RetourBatch batchRetour = shellService.runCommand(environnement, command);
        LogBatch logBatch = new LogBatch();
        logBatch.setBatch(batch);
        logBatch.setEnvironnement(environnement);
        logBatch.setDateTraitement(dateTraitement);
        if (dateTraitement == null) {
            logBatch.setDateTraitement(Date.from(Instant.now()));
        }
        logBatch.setLogDate(Calendar.getInstance().getTime());
        logBatch.setParams(realParams.toString());
        logBatch.setReturnCode(batchRetour.getReturnCode());
        logBatch.setExecutionTime(batchRetour.getExecutionTime());
        User user = userService.getUserWithAuthorities();
        logBatch.setUser(user);

        logBatchRepository.save(logBatch);

        notificationService.createNotification("[" + environnement.getApplication().getCode() + "] Le batch " + batch.getName() + " sur l'environnement " + environnement.getName() + " vient de se terminer avec le code retour " + batchRetour.getReturnCode());
        return batchRetour;
    }

    /**
     * Suppression des batchs sans environnement
     * Exécution de cette tâche au démarrage de l'application
     * puis toutes les heures (3600000ms)
     */
    @Scheduled(fixedRate = INTERVAL_CLEAN_BATCH)
    @Transactional
    public void removeBatchsWithoutEnvironnement() {
        List<Batch> lstBatchs = batchRepository.findBatchWithoutEnvironnement();
        for (Batch batch : lstBatchs) {
            logBatchRepository.deleteAllByBatchId(batch.getId());
            LOGGER.debug("Deleting not linked batch {}", batch.getName());
            batchRepository.delete(batch);
        }
    }

    @Transactional(readOnly = true)
    public Page<Batch> getAllBatchWithPredicate(Predicate predicate, Pageable pageable) {
        Predicate userPredicate = QBatch.batch.environnements.any()
                .application.usageApplications.any()
                .user.login.eq(SecurityUtils.getCurrentLogin());
        Predicate isManagerPredicate = QBatch.batch.environnements.any()
                .application.usageApplications.any()
                .canManage.isTrue();
        Predicate isUserPredicate = QBatch.batch.environnements.any()
                .application.usageApplications.any()
                .canUse.isTrue();

        Predicate finalPredicate = ExpressionUtils.allOf(predicate, userPredicate, ExpressionUtils.anyOf(isManagerPredicate, isUserPredicate));


        Page<Batch> batchPage = batchRepository.findAll(finalPredicate, pageable);
        for (Batch batch : batchPage.getContent()) {
            batch.getEnvironnements().size();
        }
        return batchPage;
    }


    @Transactional
    public Batch saveBatch(Batch batch) {
        return batchRepository.save(batch);
    }

    @Transactional(readOnly = true)
    public Batch getBatch(Long id) {
        return batchRepository.getOne(id);
    }

    @Transactional
    public void deleteBatch(Long id) {
        batchRepository.deleteById(id);
    }

    @Transactional
    public void deleteBatch(Batch batch) {
        batchRepository.delete(batch);
    }
}
