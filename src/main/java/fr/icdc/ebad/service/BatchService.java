package fr.icdc.ebad.service;

import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import fr.icdc.ebad.domain.Batch;
import fr.icdc.ebad.domain.Environnement;
import fr.icdc.ebad.domain.LogBatch;
import fr.icdc.ebad.domain.QBatch;
import fr.icdc.ebad.domain.Scheduling;
import fr.icdc.ebad.domain.User;
import fr.icdc.ebad.domain.util.RetourBatch;
import fr.icdc.ebad.repository.BatchRepository;
import fr.icdc.ebad.repository.LogBatchRepository;
import fr.icdc.ebad.repository.SchedulingRepository;
import fr.icdc.ebad.security.SecurityUtils;
import fr.icdc.ebad.service.util.EbadServiceException;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.scheduling.JobScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class BatchService {

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
    private final SchedulingRepository schedulingRepository;
    private final JobScheduler jobScheduler;

    private static final Map<Long, List<Long>> currentJob = new HashMap<>();

    public BatchService(LogBatchRepository logBatchRepository, ShellService shellService, BatchRepository batchRepository, EnvironnementService environnementService, UserService userService, NotificationService notificationService, NormeService normeService, SchedulingRepository schedulingRepository, JobScheduler jobScheduler) {
        this.logBatchRepository = logBatchRepository;
        this.shellService = shellService;
        this.batchRepository = batchRepository;
        this.environnementService = environnementService;
        this.userService = userService;
        this.notificationService = notificationService;
        this.normeService = normeService;
        this.schedulingRepository = schedulingRepository;
        this.jobScheduler = jobScheduler;
    }

    public void addJob(Long env, Long batch){
        LOGGER.debug("addJob {} on env {}", batch, env);
        List<Long> currentJobs = currentJob.getOrDefault(env, new ArrayList<>());
        currentJobs.add(batch);
        currentJob.put(env, currentJobs);
    }

    public void deleteJob(Long env, Long batch) {
        LOGGER.debug("deleteJob {} on env {}", batch, env);
        List<Long> currentJobs = currentJob.getOrDefault(env, new ArrayList<>());
        currentJobs.remove(batch);
        currentJob.put(env, currentJobs);
    }

    public List<Long> getCurrentJobForEnv(Long env) {
        LOGGER.debug("getCurrentJobForEnv on env {}",env);
        return currentJob.getOrDefault(env, new ArrayList<>());
    }


    //@Transactional
    @Job(name = "Batch %0, Env %1, Params %2, User %3", retries = 0)
    public RetourBatch jobRunBatch(Long batchId, Long environnementId, String params, String login, String uuid) throws EbadServiceException {
        addJob(environnementId, batchId);
        try {
            Batch batch = batchRepository.getById(batchId);
            if (params != null) {
                batch.setParams(params);
            }
            Environnement environnement = environnementService.getEnvironnement(environnementId);
            return runBatch(batch, environnement, login, uuid);
        }finally {
            deleteJob(environnementId, batchId);
        }
    }

//    @Transactional
    @Job(name = "Batch %0, Env %1, User %2", retries = 0)
    public RetourBatch jobRunBatch(Long batchId, Long environnementId, String login, String uuid) throws EbadServiceException {
        addJob(environnementId, batchId);
        try {
            Batch batch = batchRepository.getById(batchId);
            Environnement environnement = environnementService.getEnvironnement(environnementId);
            return runBatch(batch, environnement, login, uuid);
        }finally {
            deleteJob(environnementId, batchId);
        }
    }


//    @Transactional
    public RetourBatch runBatch(Batch batch, Environnement environnement, String login, String uuid) throws EbadServiceException {
        String params = "";
        if (null != batch.getParams()) {
            params = batch.getParams();
        }
        Date dateTraitement = environnementService.getDateTraiement(environnement.getId());
        String realParams = getParameters(environnement, params, dateTraitement);

        String command = environnement.getHomePath() + "/" + normeService.getShellPath(environnement.getNorme(), environnement.getApplication().getCode()) + environnement.getPrefix() + batch.getPath() + " " + realParams;

        LOGGER.debug("Execute batch with command : '{}'", command);

        RetourBatch batchRetour;

        try {
            batchRetour = shellService.runCommandNew(environnement, command);
        }catch (EbadServiceException e){
            User user = userService.getUser(login).orElseThrow(EbadServiceException::new);
            notificationService.createNotification("[" + environnement.getApplication().getCode() + "] Le batch " + batch.getName() + " sur l'environnement " + environnement.getName() + " n'a pas pu être lancé : "+e.getMessage(), user, true);
            throw e;
        }
        LogBatch logBatch = new LogBatch();
        if(StringUtils.hasText(uuid)) {
            logBatch.setJobId(uuid);
        }
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
        logBatch.setStdout(batchRetour.getLogOut());
        logBatch.setStderr(batchRetour.getLogErr());
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

        return batchRetour;
    }

    private String getParameters(Environnement environnement, String params, Date dateTraitement) {
        String[] paramsArray = params.split(" ");
        LOGGER.info("paramsArray = {}", paramsArray);
        StringBuilder realParams = new StringBuilder();
        for (String param : paramsArray) {
            if ("${DATE_TRAITEMENT}".equals(param)) {

                String dateParam;
                SimpleDateFormat dateFormat;
                if (StringUtils.hasText(environnement.getApplication().getDateParametrePattern())) {
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
            deleteScheduledJobFromBatch(batch.getId());
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
        return batchRepository.getById(id);
    }

    @Transactional
    public void deleteBatch(Long id) {
        logBatchRepository.deleteAllByBatchId(id);
        deleteScheduledJobFromBatch(id);
        batchRepository.deleteById(id);
    }

    @Transactional
    public void deleteScheduledJobFromBatch(Long batchId){
        List<Scheduling> schedulings = schedulingRepository.findAllByBatchId(batchId);
        schedulings.forEach(scheduling -> {
            jobScheduler.delete(String.valueOf(scheduling.getId()));
            schedulingRepository.delete(scheduling);
        });
    }

    @Transactional
    public void deleteBatch(Batch batch) {
        deleteBatch(batch.getId());
    }
}
