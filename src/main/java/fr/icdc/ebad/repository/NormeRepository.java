package fr.icdc.ebad.repository;

import fr.icdc.ebad.domain.Norme;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for the Norme entity.
 */
public interface NormeRepository extends JpaRepository<Norme, Long> {
}
