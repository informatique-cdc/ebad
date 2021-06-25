package fr.icdc.ebad.repository;

import fr.icdc.ebad.domain.Identity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdentityRepository extends JpaRepository<Identity, Long> {
    Page<Identity> findAllByAvailableApplicationId(Long applicationId, Pageable pageable);
    Page<Identity> findAllByAvailableApplicationNull(Pageable pageable);
}
