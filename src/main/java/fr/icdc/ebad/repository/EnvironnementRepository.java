package fr.icdc.ebad.repository;

import fr.icdc.ebad.domain.Environnement;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for the Environnement entity.
 */
public interface EnvironnementRepository extends JpaRepository<Environnement, Long> {
    @EntityGraph(attributePaths = {"batchs"})
    @Override
    Environnement save(Environnement environnement);
}
