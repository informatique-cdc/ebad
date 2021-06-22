package fr.icdc.ebad.repository;

import fr.icdc.ebad.domain.Identity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdentityRepository extends JpaRepository<Identity, Long> {

}
