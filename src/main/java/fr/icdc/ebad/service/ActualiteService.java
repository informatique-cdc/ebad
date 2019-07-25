package fr.icdc.ebad.service;

import fr.icdc.ebad.domain.Actualite;
import fr.icdc.ebad.repository.ActualiteRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ActualiteService {
    public static final String FIELD_ID_ACTUALITE = "id";
    private final ActualiteRepository actualiteRepository;

    public ActualiteService(ActualiteRepository actualiteRepository) {
        this.actualiteRepository = actualiteRepository;
    }

    @Transactional(readOnly = true)
    public List<Actualite> getAllActualites() {
        return actualiteRepository.findAll(new Sort(Sort.Direction.DESC, FIELD_ID_ACTUALITE));
    }

    @Transactional(readOnly = true)
    public List<Actualite> getAllActualitesPubliees() {
        return actualiteRepository.findByDraftFalse(new Sort(Sort.Direction.DESC, FIELD_ID_ACTUALITE));
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
