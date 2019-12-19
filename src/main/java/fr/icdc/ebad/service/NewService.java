package fr.icdc.ebad.service;

import fr.icdc.ebad.domain.Actualite;
import fr.icdc.ebad.repository.NewRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class NewService {
    private final NewRepository newRepository;

    public NewService(NewRepository newRepository) {
        this.newRepository = newRepository;
    }

    @Transactional(readOnly = true)
    public Page<Actualite> getAllActualites(Pageable pageable) {
        return newRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Actualite> getAllActualitesPubliees(Pageable pageable) {
        return newRepository.findByDraftFalse(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Actualite> getActualite(Long id) {
        return newRepository.findById(id);
    }

    @Transactional
    public Actualite saveActualite(Actualite actualite) {
        return newRepository.save(actualite);
    }

    @Transactional
    public void deleteActualite(Actualite actualite) {
        newRepository.deleteById(actualite.getId());
    }
}
