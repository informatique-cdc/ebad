package fr.icdc.ebad.repository;

import fr.icdc.ebad.domain.Scheduling;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchedulingRepository extends JpaRepository<Scheduling, Long> {
    Page<Scheduling> findAllByEnvironnementId(Long environmentId, Pageable pageable);
}
