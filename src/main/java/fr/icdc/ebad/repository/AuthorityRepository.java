package fr.icdc.ebad.repository;

import fr.icdc.ebad.domain.Authority;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA repository for the Authority entity.
 */
public interface AuthorityRepository extends JpaRepository<Authority, String> {
    @Override
    @EntityGraph(attributePaths = {"users"})
    Optional<Authority> findById(String id);
}
