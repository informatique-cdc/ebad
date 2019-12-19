package fr.icdc.ebad.repository;

import fr.icdc.ebad.domain.Actualite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for the New entity.
 */
public interface NewRepository extends JpaRepository<Actualite, Long> {
    Page<Actualite> findByDraftFalse(Pageable pageable);
}
