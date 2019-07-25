package fr.icdc.ebad.service;

import fr.icdc.ebad.domain.TypeFichier;
import fr.icdc.ebad.repository.TypeFichierRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TypeFichierService {
    private final TypeFichierRepository typeFichierRepository;

    public TypeFichierService(TypeFichierRepository typeFichierRepository) {
        this.typeFichierRepository = typeFichierRepository;
    }


    @Transactional(readOnly = true)
    public Page<TypeFichier> getTypeFichierFromApplication(Pageable pageable, Long applicationId) {
        return typeFichierRepository.findTypeFichierFromApplication(pageable, applicationId);
    }

    @Transactional
    public TypeFichier saveTypeFichier(TypeFichier typeFichier) {
        return typeFichierRepository.save(typeFichier);
    }

    @Transactional
    public void deleteTypeFichier(Long typeFichierId) {
        typeFichierRepository.deleteById(typeFichierId);
    }
}
