package fr.icdc.ebad.repository;

import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringPath;
import fr.icdc.ebad.domain.ApiToken;
import fr.icdc.ebad.domain.QApiToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.querydsl.binding.SingleValueBinding;

public interface ApiTokenRepository extends JpaRepository<ApiToken, Long>, QuerydslPredicateExecutor<ApiToken>, QuerydslBinderCustomizer<QApiToken> {
    @Override
    default void customize(QuerydslBindings bindings, QApiToken root) {
        bindings
                .bind(String.class)
                .first((SingleValueBinding<StringPath, String>) StringExpression::containsIgnoreCase);
    }

    Page<ApiToken> findAllByUserLogin(String login, Pageable pageable);
}
