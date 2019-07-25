package fr.icdc.ebad.repository;

import fr.icdc.ebad.domain.Directory;
import fr.icdc.ebad.domain.Environnement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data JPA repository for the Directory entity.
 */
public interface DirectoryRepository extends JpaRepository<Directory, Long> {
    @Query("select directory from Directory directory left join directory.environnement environnement where environnement.id = :environnement")
    Page<Directory> findDirectoryFromEnvironnement(Pageable pageable, @Param("environnement") Long environnement);

    void deleteByEnvironnement(Environnement environnement);
}
