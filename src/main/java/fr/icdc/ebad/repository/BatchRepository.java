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

import java.util.List;

/**
 * Spring Data JPA repository for the Batch entity.
 */
public interface BatchRepository extends JpaRepository<Batch, Long>, QuerydslPredicateExecutor<Batch>, QuerydslBinderCustomizer<QBatch> {
    @EntityGraph(attributePaths = {"environnements"})
    @Override
    Iterable<Batch> findAll(Predicate predicate);

    @Override
    Page<Batch> findAll(Predicate predicate, Pageable pageable);

    @EntityGraph(attributePaths = {"environnements"})
    @Override
    Batch getById(Long id);

    @Query("select batch from Batch batch where batch.environnements is empty")
    List<Batch> findBatchWithoutEnvironnement();

    default void customize(
            QuerydslBindings bindings, QBatch root) {
        bindings.bind(String.class)
                .first((SingleValueBinding<StringPath, String>) StringExpression::containsIgnoreCase);
    }
}
