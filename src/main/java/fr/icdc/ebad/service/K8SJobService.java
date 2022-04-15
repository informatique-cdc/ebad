package fr.icdc.ebad.service;

import fr.icdc.ebad.domain.K8SJob;
import fr.icdc.ebad.repository.K8SJobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class K8SJobService {

    private static final Logger LOGGER = LoggerFactory.getLogger(K8SJobService.class);

    private final K8SJobRepository k8SJobRepository;

    public K8SJobService(K8SJobRepository k8SJobRepository) {
        this.k8SJobRepository = k8SJobRepository;
    }

    @Transactional
    public K8SJob saveK8SJob(K8SJob k8SJob) {
        return k8SJobRepository.save(k8SJob);
    }
}
