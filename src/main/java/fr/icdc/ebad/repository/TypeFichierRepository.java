package fr.icdc.ebad.repository;

import fr.icdc.ebad.domain.Application;
import fr.icdc.ebad.domain.TypeFichier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data JPA repository for the TypeFichier entity.
 */
public interface TypeFichierRepository extends JpaRepository<TypeFichier, Long> {
    @Query("select typeFichier from TypeFichier typeFichier left join typeFichier.application application where application.id = :application")
    Page<TypeFichier> findTypeFichierFromApplication(Pageable pageable, @Param("application") Long application);

    void deleteByApplication(Application application);
}
