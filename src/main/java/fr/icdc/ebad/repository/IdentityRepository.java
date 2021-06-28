package fr.icdc.ebad.repository;

import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringPath;
import fr.icdc.ebad.domain.Identity;
import fr.icdc.ebad.domain.QIdentity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.querydsl.binding.SingleValueBinding;

public interface IdentityRepository extends JpaRepository<Identity, Long>, QuerydslPredicateExecutor<Identity>, QuerydslBinderCustomizer<QIdentity> {
    @Override
    default void customize(QuerydslBindings bindings, QIdentity root) {
        bindings
                .bind(String.class)
                .first((SingleValueBinding<StringPath, String>) StringExpression::containsIgnoreCase);
    }
}
