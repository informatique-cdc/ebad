package fr.icdc.ebad.service;

import fr.icdc.ebad.repository.ApplicationRepository;
import fr.icdc.ebad.repository.BatchRepository;
import fr.icdc.ebad.repository.LogBatchRepository;
import fr.icdc.ebad.repository.UserRepository;
import fr.icdc.ebad.web.rest.dto.StatisticsDto;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static javax.management.timer.Timer.ONE_HOUR;

/**
 * Ce service permet de generer les statistiques d'utilisation de l'application
 * nombre de batchs, nombre d'application, nombre de visiteurs, ...
 *
 * @author dtrouillet
 */
@Service
public class StatistiquesService {
    private final ApplicationRepository applicationRepository;
    private final BatchRepository batchRepository;
    private final LogBatchRepository logBatchRepository;
    private final UserRepository userRepository;

    public StatistiquesService(ApplicationRepository applicationRepository, BatchRepository batchRepository, LogBatchRepository logBatchRepository, UserRepository userRepository) {
        this.applicationRepository = applicationRepository;
        this.batchRepository = batchRepository;
        this.logBatchRepository = logBatchRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    @Cacheable("stats")
    public StatisticsDto generationStatistiques() {
        StatisticsDto statistiquesDto = new StatisticsDto();
        statistiquesDto.setApplicationsNbr(applicationRepository.count());
        statistiquesDto.setAvgExecutionTime(logBatchRepository.avgTime());
        statistiquesDto.setBatchsNbrs(batchRepository.count());
        statistiquesDto.setBatchsRunnedNbrs(logBatchRepository.count());
        Instant instant = Instant.now().minus(30, ChronoUnit.DAYS);
        statistiquesDto.setStatisticsByDay(logBatchRepository.countBatchByDay(Date.from(instant)));
        statistiquesDto.setUsersNbr(userRepository.count());
        return statistiquesDto;
    }

    @Scheduled(fixedDelay = ONE_HOUR)
    @CacheEvict("stats")
    public void evictCache() {
        //noop
    }
}
