package fr.icdc.ebad.repository;

import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringPath;
import fr.icdc.ebad.domain.AccreditationRequest;
import fr.icdc.ebad.domain.QAccreditationRequest;
import fr.icdc.ebad.domain.StateRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.querydsl.binding.SingleValueBinding;

import java.util.Optional;

public interface AccreditationRequestRepository extends JpaRepository<AccreditationRequest, Long>, QuerydslPredicateExecutor<AccreditationRequest>, QuerydslBinderCustomizer<QAccreditationRequest> {
    @Override
    default void customize(QuerydslBindings bindings, QAccreditationRequest root) {
        bindings
                .bind(String.class)
                .first((SingleValueBinding<StringPath, String>) StringExpression::containsIgnoreCase);
    }

    Optional<AccreditationRequest> findByIdAndState(Long id, StateRequest state);
}
