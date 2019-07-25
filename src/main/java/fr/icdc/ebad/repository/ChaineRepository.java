package fr.icdc.ebad.repository;

import fr.icdc.ebad.domain.Chaine;
import fr.icdc.ebad.domain.Environnement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data JPA repository for the Chaine entity.
 */
public interface ChaineRepository extends JpaRepository<Chaine, Long> {
    @EntityGraph(attributePaths = {"chaineAssociations", "chaineAssociations.batch.environnements"})
    @Query("select chaine from Chaine chaine left join chaine.environnement environnement where environnement.id = :environnement")
    Page<Chaine> findChaineFromEnvironnement(Pageable pageable, @Param("environnement") Long environnement);

    void deleteByEnvironnement(Environnement environnement);
}
