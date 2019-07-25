package fr.icdc.ebad.repository;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringPath;
import fr.icdc.ebad.domain.Batch;
import fr.icdc.ebad.domain.QBatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.querydsl.binding.SingleValueBinding;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring Data JPA repository for the Batch entity.
 */
public interface BatchRepository extends JpaRepository<Batch, Long>, QuerydslPredicateExecutor<Batch>, QuerydslBinderCustomizer<QBatch> {
    @EntityGraph(attributePaths = {"environnements"})
    @Override
    Iterable<Batch> findAll(Predicate predicate);

    @EntityGraph(attributePaths = {"environnements"})
    @Override
    Batch getOne(Long id);

    @EntityGraph(attributePaths = {"environnements", "environnements.application", "environnements.batchs",
            "environnements.batchs.chaineAssociations", "environnements.logBatchs"})
    @Query("select batch from Batch batch left join batch.environnements environnements where environnements.id = :environnement")
    Page<Batch> findBatchFromEnvironnement(Pageable pageable, @Param("environnement") Long environnement);


    @Query("select batch from Batch batch where batch.environnements is empty")
    List<Batch> findBatchWithoutEnvironnement();

    default void customize(
            QuerydslBindings bindings, QBatch root) {
        bindings.bind(String.class)
                .first((SingleValueBinding<StringPath, String>) StringExpression::containsIgnoreCase);
    }
}
