package fr.icdc.ebad.repository;

import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringPath;
import fr.icdc.ebad.domain.Environnement;
import fr.icdc.ebad.domain.LogBatch;
import fr.icdc.ebad.domain.QLogBatch;
import fr.icdc.ebad.web.rest.dto.StatisticByDayDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.querydsl.binding.SingleValueBinding;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the Application entity.
 */
public interface LogBatchRepository extends JpaRepository<LogBatch, Long>, QuerydslPredicateExecutor<LogBatch>, QuerydslBinderCustomizer<QLogBatch> {
    @Override
    default void customize(QuerydslBindings bindings, QLogBatch root) {
        bindings.bind(String.class).first((SingleValueBinding<StringPath, String>) StringExpression::containsIgnoreCase);
    }

    @Query("select new fr.icdc.ebad.web.rest.dto.StatisticByDayDto(" +
            "CONCAT(function('to_char',logBatch.logDate,'YYYY-MM-DD'),''), " +
            "count(logBatch), " +
            "avg(logBatch.executionTime)" +
            ")" +
            " from LogBatch logBatch " +
            "where logBatch.logDate  >= :date " +
            "group by function('to_char',logBatch.logDate,'YYYY-MM-DD') " +
            "order by function('to_char',logBatch.logDate,'YYYY-MM-DD') desc")
    List<StatisticByDayDto> countBatchByDay(@Param("date") Date date);

    @Query("select avg(logBatch.executionTime) from LogBatch logBatch")
    Long avgTime();

    void deleteAllByBatchId(Long id);

    Optional<LogBatch> getByJobId(String jobId);

    void deleteByEnvironnement(Environnement environnement);
}
