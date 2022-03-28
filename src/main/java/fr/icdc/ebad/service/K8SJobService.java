package fr.icdc.ebad.service;

import fr.icdc.ebad.repository.K8SJobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class K8SJobService {

    private static final Logger LOGGER = LoggerFactory.getLogger(K8SJobService.class);

    private final K8SJobRepository k8SJobRepository;

    public K8SJobService(K8SJobRepository k8SJobRepository) {
        this.k8SJobRepository = k8SJobRepository;
    }
}
