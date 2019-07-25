package fr.icdc.ebad.service;

import fr.icdc.ebad.domain.LogBatch;
import fr.icdc.ebad.repository.LogBatchRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Created by dtrouillet on 27/06/2019.
 */
@Service
public class LogBatchService {

    private final LogBatchRepository logBatchRepository;

    public LogBatchService(LogBatchRepository logBatchRepository) {
        this.logBatchRepository = logBatchRepository;
    }


    @Transactional(readOnly = true)
    public Page<LogBatch> getAllLogBatchWithPageable(Pageable pageable) {
        return logBatchRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<LogBatch> getAllLogBatchFromEnvironmentWithPageable(Pageable pageable, Long environnementId) {
        return logBatchRepository.findByEnvironnement(pageable, environnementId, null);
    }

    @Transactional(readOnly = true)
    public Page<LogBatch> getAllLogBatchFromEnvironmentAndBatchWithPageable(Pageable pageable, Long environnementId, Long batchId) {
        return logBatchRepository.findByEnvironnement(pageable, environnementId, batchId);
    }

    @Transactional
    public LogBatch saveLogBatch(LogBatch logBatch) {
        return logBatchRepository.save(logBatch);
    }
}
