package fr.icdc.ebad.repository;

import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringPath;
import fr.icdc.ebad.domain.Norme;
import fr.icdc.ebad.domain.QNorme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.querydsl.binding.SingleValueBinding;


/**
 * Spring Data JPA repository for the Norme entity.
 */
public interface NormeRepository extends JpaRepository<Norme, Long>, QuerydslPredicateExecutor<Norme>, QuerydslBinderCustomizer<QNorme> {
    @Override
    default void customize(QuerydslBindings bindings, QNorme root) {
        bindings
                .bind(String.class)
                .first((SingleValueBinding<StringPath, String>) StringExpression::containsIgnoreCase);
    }
}


