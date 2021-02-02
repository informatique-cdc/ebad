package fr.icdc.ebad.service;

import fr.icdc.ebad.domain.Batch;
import fr.icdc.ebad.domain.Environnement;
import fr.icdc.ebad.domain.Scheduling;
import fr.icdc.ebad.repository.BatchRepository;
import fr.icdc.ebad.repository.EnvironnementRepository;
import fr.icdc.ebad.repository.SchedulingRepository;
import fr.icdc.ebad.service.scheduling.RunnableBatch;
import fr.icdc.ebad.service.util.EbadServiceException;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

@Service
public class SchedulingService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulingService.class);
    private final Map<Long, ScheduledFuture<?>> tasks = new HashMap<>();
    private final ThreadPoolTaskScheduler taskScheduler;
    private final BatchRepository batchRepository;
    private final EnvironnementRepository environnementRepository;
    private final SchedulingRepository schedulingRepository;

    public SchedulingService(ThreadPoolTaskScheduler taskScheduler, BatchRepository batchRepository, EnvironnementRepository environnementRepository, SchedulingRepository schedulingRepository) {
        this.taskScheduler = taskScheduler;
        this.batchRepository = batchRepository;
        this.environnementRepository = environnementRepository;
        this.schedulingRepository = schedulingRepository;
    }

    @PostConstruct
    private void initScheduling() throws EbadServiceException {
        LOGGER.info("START INIT SCHEDULING");
        List<Scheduling> schedulings = schedulingRepository.findAll();
        for (Scheduling scheduling : schedulings) {
            run(scheduling);
        }
    }

    @Lookup
    public RunnableBatch getRunnableBatch() {
        return null;
    }

    @Transactional
    public Page<Scheduling> listByEnvironment(Long environmentId, Pageable pageable) {
        return schedulingRepository.findAllByEnvironnementId(environmentId, pageable);
    }

    @Transactional
    public Page<Scheduling> listAll(Pageable pageable) {
        return schedulingRepository.findAll(pageable);
    }

    @Transactional
    public Scheduling saveAndRun(Long batchId, Long environnementId, String parameters, String cron) throws EbadServiceException {
        Batch batch = batchRepository.getOne(batchId);
        Environnement environnement = environnementRepository.getOne(environnementId);

        Scheduling newScheduling = Scheduling.builder()
                .batch(batch)
                .parameters(parameters)
                .cron(cron)
                .environnement(environnement)
                .build();

        Scheduling scheduling = schedulingRepository.save(newScheduling);
        run(scheduling);
        Hibernate.initialize(scheduling.getBatch());
        Hibernate.initialize(scheduling.getEnvironnement());
        return scheduling;
    }

    @Transactional
    public void remove(Long schedulingId) {
        Scheduling scheduling = schedulingRepository.getOne(schedulingId);
        ScheduledFuture<?> scheduledFuture = tasks.get(schedulingId);
        scheduledFuture.cancel(false);
        tasks.remove(schedulingId);
        schedulingRepository.delete(scheduling);
    }

    public void run(Scheduling scheduling) throws EbadServiceException {
        if (scheduling.getBatch() == null || scheduling.getEnvironnement() == null) {
            throw new EbadServiceException("Batch or environment doesn't exist");
        }

        RunnableBatch runnableBatch = getRunnableBatch();
        runnableBatch.setScheduling(scheduling);

        CronTrigger cronTrigger = new CronTrigger(scheduling.getCron());
        ScheduledFuture<?> scheduledFuture = taskScheduler.schedule(
                runnableBatch,
                cronTrigger
        );

        tasks.put(scheduling.getId(), scheduledFuture);
    }

    @Transactional
    public Scheduling get(Long schedulingId) {
        return schedulingRepository.getOne(schedulingId);
    }
}
