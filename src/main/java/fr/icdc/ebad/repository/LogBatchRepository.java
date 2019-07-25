package fr.icdc.ebad.repository;

import fr.icdc.ebad.domain.Environnement;
import fr.icdc.ebad.domain.LogBatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring Data JPA repository for the Application entity.
 */
public interface LogBatchRepository extends JpaRepository<LogBatch, Long> {
    @Query("select logBatch from LogBatch logBatch left join logBatch.environnement environnement left join logBatch.batch batch left join logBatch.user user where environnement.id = :environnementId and (:batchId is null or batch.id = :batchId) order by logBatch.id desc")
    Page<LogBatch> findByEnvironnement(Pageable pageable, @Param("environnementId") Long environnmentId, @Param("batchId") Long batchId);

    @Query("select logBatch from LogBatch logBatch left join logBatch.batch batch where batch.id = :batchId")
    List<LogBatch> findAllByBatchId(@Param("batchId") Long batchId);

    @Query("select count(logBatch) from LogBatch logBatch group by substring(logBatch.logDate,1,10) order by substring(logBatch.logDate,1,10) desc")
    List<Long> countBatchByDay(Pageable pageable);

    @Query("select avg(logBatch.executionTime) from LogBatch logBatch")
    Long avgTime();

    void deleteAllByBatchId(Long id);

    void deleteByEnvironnement(Environnement environnement);
}
