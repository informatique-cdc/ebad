package fr.icdc.ebad.service;

import fr.icdc.ebad.repository.ApplicationRepository;
import fr.icdc.ebad.repository.BatchRepository;
import fr.icdc.ebad.repository.LogBatchRepository;
import fr.icdc.ebad.repository.UserRepository;
import fr.icdc.ebad.web.rest.dto.StatistiquesDto;
import fr.icdc.ebad.web.rest.util.PaginationUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ce service permet de generer les statistiques d'utilisation de l'application
 * nombre de batchs, nombre d'application, nombre de visiteurs, ...
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
    public StatistiquesDto generationStatistiques() {
        StatistiquesDto statistiquesDto = new StatistiquesDto();
        statistiquesDto.setNbrApplications(applicationRepository.count());
        statistiquesDto.setNbrBatchs(batchRepository.count());
        statistiquesDto.setNbrBatchsLances(logBatchRepository.count());
        statistiquesDto.setTpsMoyenBatch(logBatchRepository.avgTime());
        statistiquesDto.setNbrUtilisateurs(userRepository.count());
        statistiquesDto.setNbrBatchLancesParJour(logBatchRepository.countBatchByDay(PaginationUtil.generatePageRequest(0, 10)));
        return statistiquesDto;
    }
}
