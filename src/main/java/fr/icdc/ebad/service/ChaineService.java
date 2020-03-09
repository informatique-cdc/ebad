package fr.icdc.ebad.service;

import com.jcraft.jsch.JSchException;
import com.querydsl.core.types.Predicate;
import fr.icdc.ebad.domain.Chaine;
import fr.icdc.ebad.domain.ChaineAssociation;
import fr.icdc.ebad.domain.Environnement;
import fr.icdc.ebad.domain.QChaine;
import fr.icdc.ebad.domain.util.RetourBatch;
import fr.icdc.ebad.repository.ChaineRepository;
import fr.icdc.ebad.security.SecurityUtils;
import fr.icdc.ebad.service.util.EbadServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

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

    @Transactional
    public RetourBatch runChaine(Long id) throws IOException, JSchException, EbadServiceException {
        Chaine chaine = getChaine(id);
        return runChaine(chaine);
    }

    //TODO Gestion des retours
    //TODO Modification des parametres de chaque batch à l'enregistrement de la chaine?
    public RetourBatch runChaine(Chaine chaine) throws IOException, JSchException, EbadServiceException {
        LOGGER.debug("runChaine {}", chaine);
        RetourBatch retourChaine = new RetourBatch();
        retourChaine.setExecutionTime(0L);
        retourChaine.setLogOut("");
        RetourBatch retourBatch;
        for (ChaineAssociation chaineAssociation : chaine.getChaineAssociations()) {
            chaineAssociation.getBatch().setParams(chaineAssociation.getBatch().getDefaultParam());
            retourBatch = batchService.runBatch(chaineAssociation.getBatch(), chaine.getEnvironnement());
            retourChaine.setLogOut(retourChaine.getLogOut().concat(" " + retourBatch.getLogOut()));
            retourChaine.setExecutionTime(retourChaine.getExecutionTime() + retourBatch.getExecutionTime());
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
        return chaineRepository.getOne(id);
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
