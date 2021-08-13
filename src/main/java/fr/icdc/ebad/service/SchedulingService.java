package fr.icdc.ebad.service;

import fr.icdc.ebad.domain.Batch;
import fr.icdc.ebad.domain.Environnement;
import fr.icdc.ebad.domain.Scheduling;
import fr.icdc.ebad.repository.BatchRepository;
import fr.icdc.ebad.repository.EnvironnementRepository;
import fr.icdc.ebad.repository.SchedulingRepository;
import fr.icdc.ebad.service.util.EbadServiceException;
import org.hibernate.Hibernate;
import org.jobrunr.scheduling.JobScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SchedulingService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulingService.class);
    private final BatchRepository batchRepository;
    private final BatchService batchService;
    private final EnvironnementRepository environnementRepository;
    private final SchedulingRepository schedulingRepository;
    private final JobScheduler jobScheduler;

    public SchedulingService(BatchRepository batchRepository, BatchService batchService, EnvironnementRepository environnementRepository, SchedulingRepository schedulingRepository, JobScheduler jobScheduler) {
        this.batchRepository = batchRepository;
        this.batchService = batchService;
        this.environnementRepository = environnementRepository;
        this.schedulingRepository = schedulingRepository;
        this.jobScheduler = jobScheduler;
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
        Batch batch = batchRepository.getById(batchId);
        Environnement environnement = environnementRepository.getById(environnementId);

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
        Scheduling scheduling = schedulingRepository.getById(schedulingId);
        jobScheduler.delete(String.valueOf(scheduling.getId()));
        schedulingRepository.delete(scheduling);
    }

    public void run(Scheduling scheduling) throws EbadServiceException {
        if (scheduling.getBatch() == null || scheduling.getEnvironnement() == null) {
            throw new EbadServiceException("Batch or environment doesn't exist");
        }

        String id;
        if (scheduling.getParameters() == null) {
            id = jobScheduler.scheduleRecurrently(String.valueOf(scheduling.getId()), scheduling.getCron(), () -> batchService.jobRunBatch(scheduling.getBatch().getId(), scheduling.getEnvironnement().getId(), "ebad", ""));
        } else {
            id = jobScheduler.scheduleRecurrently(String.valueOf(scheduling.getId()), scheduling.getCron(), () -> batchService.jobRunBatch(scheduling.getBatch().getId(), scheduling.getEnvironnement().getId(), scheduling.getParameters(), "ebad", ""));
        }
        LOGGER.warn("id is {}", id);
    }

    @Transactional
    public Scheduling get(Long schedulingId) {
        return schedulingRepository.getById(schedulingId);
    }
}
