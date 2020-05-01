package fr.icdc.ebad.repository;

import fr.icdc.ebad.domain.UsageApplication;
import fr.icdc.ebad.domain.UsageApplicationId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsageApplicationRepository extends JpaRepository<UsageApplication, UsageApplicationId> {
    @EntityGraph(attributePaths = {"application", "user"})
    Page<UsageApplication> findAllByApplicationId(Long applicationId, Pageable pageable);
}
