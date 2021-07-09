package fr.icdc.ebad.service;

import com.querydsl.core.types.Predicate;
import fr.icdc.ebad.domain.LogBatch;
import fr.icdc.ebad.repository.LogBatchRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


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
    public Page<LogBatch> getAllLogBatchWithPageable(Predicate predicate, Pageable pageable) {
        return logBatchRepository.findAll(predicate, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<LogBatch> getByJobId(String jobId) {
        return logBatchRepository.getByJobId(jobId);
    }

}
