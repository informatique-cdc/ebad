package fr.icdc.ebad.repository;

import fr.icdc.ebad.domain.Actualite;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data JPA repository for the Actualite entity.
 */
public interface ActualiteRepository extends JpaRepository<Actualite, Long> {
    List<Actualite> findByDraftFalse(Sort sort);
}
