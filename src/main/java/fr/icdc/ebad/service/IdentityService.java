package fr.icdc.ebad.service;

import fr.icdc.ebad.domain.Identity;
import fr.icdc.ebad.repository.IdentityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class IdentityService {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdentityService.class);

    private final IdentityRepository identityRepository;

    public IdentityService(IdentityRepository identityRepository) {
        this.identityRepository = identityRepository;
    }

    @Transactional
    public Identity saveIdentity(Identity identity) {
        return identityRepository.save(identity);
    }

    @Transactional(readOnly = true)
    public Optional<Identity> getIdentity(Long id) {
        return identityRepository.findById(id);
    }

    @Transactional
    public void deleteIdentity(Long id) {
        identityRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<Identity> findAll(Pageable pageable) {
        return identityRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Identity> findAllByApplication(Long applicationId, Pageable pageable) {
        return identityRepository.findAllByAvailableApplicationId(applicationId, pageable);
    }
}
