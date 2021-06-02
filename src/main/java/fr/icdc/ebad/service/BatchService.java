package fr.icdc.ebad.service;

import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import fr.icdc.ebad.domain.*;
import fr.icdc.ebad.domain.util.RetourBatch;
import fr.icdc.ebad.repository.BatchRepository;
import fr.icdc.ebad.repository.LogBatchRepository;
import fr.icdc.ebad.security.SecurityUtils;
import fr.icdc.ebad.service.util.EbadServiceException;

import org.jobrunr.jobs.annotations.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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
    private final SimpMessagingTemplate messagingTemplate;

    public BatchService(LogBatchRepository logBatchRepository, ShellService shellService, BatchRepository batchRepository, EnvironnementService environnementService, UserService userService, NotificationService notificationService, NormeService normeService, SimpMessagingTemplate messagingTemplate) {
        this.logBatchRepository = logBatchRepository;
        this.shellService = shellService;
        this.batchRepository = batchRepository;
        this.environnementService = environnementService;
        this.userService = userService;
        this.notificationService = notificationService;
        this.normeService = normeService;
        this.messagingTemplate = messagingTemplate;
    }


    @Transactional
    @Job(name = "Batch %0, Env %1, Params %2, User %3", retries = 0)
    public RetourBatch jobRunBatch(Long batchId, Long environnementId, String params, String login) throws EbadServiceException {
//        System.out.println("DAMIEN SEND MESSAGE TO /topic/env/"+environnementId);
//        this.messagingTemplate.convertAndSend("/topic/env/"+environnementId, batchId+" RUN");
        Batch batch = batchRepository.getOne(batchId);
        if (params != null) {
            batch.setParams(params);
        }
        Environnement environnement = environnementService.getEnvironnement(environnementId);
        return runBatch(batch, environnement, login);
    }

    @Transactional
    @Job(name = "Batch %0, Env %1, User %2", retries = 0)
    public RetourBatch jobRunBatch(Long batchId, Long environnementId, String login) throws EbadServiceException {
//        System.out.println("DAMIEN SEND MESSAGE TO /topic/env/"+environnementId);
//        this.messagingTemplate.convertAndSend("/topic/env/"+environnementId, batchId+" RUN");
        Batch batch = batchRepository.getOne(batchId);
        Environnement environnement = environnementService.getEnvironnement(environnementId);
        return runBatch(batch, environnement, login);
    }


    @Transactional
    public RetourBatch runBatch(Batch batch, Environnement environnement, String login) throws EbadServiceException {
        System.out.println("DAMIEN SEND MESSAGE TO /topic/env/"+environnement.getId());
        this.messagingTemplate.convertAndSend("/topic/env/"+environnement.getId(), batch.getId()+" RUN");

        String params = "";
        if (null != batch.getParams()) {
            params = batch.getParams();
        }
        Date dateTraitement = environnementService.getDateTraiement(environnement.getId());
        String realParams = getParameters(environnement, params, dateTraitement);

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
        logBatch.setParams(realParams);
        logBatch.setReturnCode(batchRetour.getReturnCode());
        logBatch.setExecutionTime(batchRetour.getExecutionTime());
        try {
            User user = userService.getUser(login).orElseThrow(EbadServiceException::new);
            logBatch.setUser(user);
            logBatchRepository.save(logBatch);
            notificationService.createNotification("[" + environnement.getApplication().getCode() + "] Le batch " + batch.getName() + " sur l'environnement " + environnement.getName() + " vient de se terminer avec le code retour " + batchRetour.getReturnCode(), user, batchRetour.getReturnCode() != 0);
        } catch (EbadServiceException e) {
            Optional<User> user = userService.getUser("ebad");
            logBatch.setUser(user.orElseThrow());
            logBatchRepository.save(logBatch);
        }

        System.out.println("DAMIEN FINISH    MESSAGE TO /topic/env/"+environnement.getId());
        this.messagingTemplate.convertAndSend("/topic/env/"+environnement.getId(), batch.getId()+" FINISH");

        return batchRetour;
    }

    private String getParameters(Environnement environnement, String params, Date dateTraitement) {
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
        return realParams.toString();
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
        logBatchRepository.deleteAllByBatchId(id);
        batchRepository.deleteById(id);
    }

    @Transactional
    public void deleteBatch(Batch batch) {
        logBatchRepository.deleteAllByBatchId(batch.getId());
        batchRepository.delete(batch);
    }
}
