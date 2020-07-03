package fr.icdc.ebad.service;

import com.querydsl.core.types.Predicate;
import fr.icdc.ebad.domain.Application;
import fr.icdc.ebad.domain.Norme;
import fr.icdc.ebad.domain.QApplication;
import fr.icdc.ebad.repository.NormeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


/**
 * Created by dtrouillet on 27/06/2019.
 */
@Service
public class NormeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NormeService.class);
    private final NormeRepository normeRepository;
    private final ApplicationService applicationService;

    public NormeService(NormeRepository normeRepository, ApplicationService applicationService) {
        this.normeRepository = normeRepository;
        this.applicationService = applicationService;
    }

    @Transactional(readOnly = true)
    public Optional<Norme> findNormeById(Long normeId) {
        return normeRepository.findById(normeId);
    }

    @Transactional
    public Norme saveNorme(Norme norme) {
        return normeRepository.saveAndFlush(norme);
    }

    @Transactional
    public void deleteNormeById(Long normeId) {
        Predicate applicationPredicate = QApplication.application.environnements.any().norme.id.eq(normeId);
        List<Application> applicationsWithNorme = applicationService.findApplication(applicationPredicate);
        if (!applicationsWithNorme.isEmpty()) {
            LOGGER.error("Norme {} utilisée par un ou plusieurs environnements: Suppression impossible", normeId);
            throw new IllegalStateException("Norme " + normeId + " utilisée par un ou plusieurs environnements: Suppression impossible");
        }
        normeRepository.deleteById(normeId);
    }

    @Transactional(readOnly = true)
    public Page<Norme> getAllNormes(Predicate predicate, Pageable pageable) {
        return normeRepository.findAll(predicate, pageable);
    }

    public String getShellPath(Norme norme, String appCode) {
        return norme.getPathShell().replace("$APP_CODE$", appCode);
    }
}
