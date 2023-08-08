package fr.icdc.ebad.service;

import com.querydsl.core.types.Predicate;
import fr.icdc.ebad.domain.Chaine;
import fr.icdc.ebad.domain.ChaineAssociation;
import fr.icdc.ebad.domain.Environnement;
import fr.icdc.ebad.domain.QChaine;
import fr.icdc.ebad.domain.util.RetourBatch;
import fr.icdc.ebad.repository.ChaineRepository;
import fr.icdc.ebad.security.SecurityUtils;
import fr.icdc.ebad.service.util.EbadServiceException;
import org.jobrunr.jobs.annotations.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Created by dtrouillet on 03/03/2016.
 */
@Service
public class ChaineService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChaineService.class);

    private final BatchService batchService;
    private final ChaineRepository chaineRepository;

    public ChaineService(BatchService batchService, ChaineRepository chaineRepository) {
        this.batchService = batchService;
        this.chaineRepository = chaineRepository;
    }

    @Job(name = "Chain %0, User %1", retries = 0)
    public RetourBatch jobRunChaine(Long chaineId, String login) throws EbadServiceException {
        Chaine chaine = getChaine(chaineId);
        return runChaine(chaine, login);
    }

    //TODO Gestion des retours
    //TODO Modification des parametres de chaque batch à l'enregistrement de la chaine?
    public RetourBatch runChaine(Chaine chaine, String login) throws EbadServiceException {
        LOGGER.debug("runChaine {}", chaine);
        RetourBatch retourChaine = new RetourBatch();
        retourChaine.setExecutionTime(0L);
        retourChaine.setLogOut("");
        RetourBatch retourBatch;
        for (ChaineAssociation chaineAssociation : chaine.getChaineAssociations()) {
            UUID uuid = UUID.randomUUID();
            batchService.addJob(chaine.getEnvironnement().getId(), chaineAssociation.getBatch().getId());
            try {
                chaineAssociation.getBatch().setParams(chaineAssociation.getBatch().getDefaultParam());
                retourBatch = batchService.runBatch(chaineAssociation.getBatch(), chaine.getEnvironnement(), login, uuid.toString());
                retourChaine.setLogOut(retourChaine.getLogOut().concat(" " + retourBatch.getLogOut()));
                retourChaine.setExecutionTime(retourChaine.getExecutionTime() + retourBatch.getExecutionTime());
            }finally {
                batchService.deleteJob(chaine.getEnvironnement().getId(), chaineAssociation.getBatch().getId());
            }
            if (retourBatch.getReturnCode() > 0) {
                retourChaine.setReturnCode(retourBatch.getReturnCode());
                return retourChaine;
            }
        }
        return retourChaine;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Chaine addChaine(Chaine chaine){
        int order = 0;

        for(ChaineAssociation chaineAssociation : chaine.getChaineAssociations()){
            chaineAssociation.setBatchOrder(order);
            chaineAssociation.setChaine(chaine);
            chaineAssociation.setBatch(batchService.getBatch(chaineAssociation.getBatch().getId()));
            order++;
        }
        chaine.setCreatedBy(SecurityUtils.getCurrentLogin());
        return chaineRepository.saveAndFlush(chaine);
    }

    @Transactional(readOnly = true)
    public Chaine getChaine(Long id) {
        return chaineRepository.getReferenceById(id);
    }

    @Transactional(readOnly = true)
    public Page<Chaine> getAllChaineFromEnvironmentWithPageable(Predicate predicate, Pageable pageable, Environnement environnement) {
        Predicate newPredicate = QChaine.chaine.environnement.id.eq(environnement.getId()).and(predicate);
        return chaineRepository.findAll(newPredicate, pageable);
    }

    @Transactional
    public void deleteChaine(Long id) {
        chaineRepository.deleteById(id);
    }

    @Transactional
    public Chaine saveChaine(Chaine chaine) {
        return chaineRepository.save(chaine);
    }

    @Transactional
    public Chaine updateChaine(Chaine chaine) {
        int order = 0;

        for (ChaineAssociation chaineAssociation : chaine.getChaineAssociations()) {
            chaineAssociation.setBatchOrder(order);
            chaineAssociation.setChaine(chaine);
            chaineAssociation.setBatch(batchService.getBatch(chaineAssociation.getBatch().getId()));
            order++;
        }

        return saveChaine(chaine);
    }
}
