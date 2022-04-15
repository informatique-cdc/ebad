package fr.icdc.ebad.repository;

import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringPath;
import fr.icdc.ebad.domain.Batch;
import fr.icdc.ebad.domain.K8SJob;
import fr.icdc.ebad.domain.QK8SJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.querydsl.binding.SingleValueBinding;

/**
 * Spring Data JPA repository for the K8S Job entity.
 */
public interface K8SJobRepository extends JpaRepository<K8SJob, Long>, QuerydslPredicateExecutor<Batch>, QuerydslBinderCustomizer<QK8SJob> {


    default void customize(
            QuerydslBindings bindings, QK8SJob root) {
        bindings.bind(String.class)
                .first((SingleValueBinding<StringPath, String>) StringExpression::containsIgnoreCase);
    }
}
