package fr.icdc.ebad.service;

import fr.icdc.ebad.domain.Actualite;
import fr.icdc.ebad.repository.ActualiteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class NewService {
    public static final String FIELD_ID_ACTUALITE = "id";
    private final ActualiteRepository actualiteRepository;

    public NewService(ActualiteRepository actualiteRepository) {
        this.actualiteRepository = actualiteRepository;
    }

    @Transactional(readOnly = true)
    public Page<Actualite> getAllActualites(Pageable pageable) {
        return actualiteRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Actualite> getAllActualitesPubliees(Pageable pageable) {
        return actualiteRepository.findByDraftFalse(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Actualite> getActualite(Long id) {
        return actualiteRepository.findById(id);
    }

    @Transactional
    public Actualite saveActualite(Actualite actualite) {
        return actualiteRepository.save(actualite);
    }

    @Transactional
    public void deleteActualite(Actualite actualite) {
        actualiteRepository.deleteById(actualite.getId());
    }
}
