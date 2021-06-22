package fr.icdc.ebad.service;

import fr.icdc.ebad.domain.Batch;
import fr.icdc.ebad.domain.Identity;
import fr.icdc.ebad.repository.IdentityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    public Identity getIdentity(Long id) {
        return identityRepository.getById(id);
    }

    @Transactional
    public void deleteIdentity(Long id) {
        identityRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Identity> findAll() {
        return identityRepository.findAll();
    }
}
