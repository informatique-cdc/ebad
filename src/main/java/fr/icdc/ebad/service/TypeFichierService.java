package fr.icdc.ebad.service;

import com.querydsl.core.types.Predicate;
import fr.icdc.ebad.domain.QTypeFichier;
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
    public Page<TypeFichier> getTypeFichierFromApplication(Predicate predicate, Pageable pageable, Long applicationId) {
        Predicate newPredicate = QTypeFichier.typeFichier.application.id.eq(applicationId).and(predicate);
        return typeFichierRepository.findAll(newPredicate, pageable);
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
