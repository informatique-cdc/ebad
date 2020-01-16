package fr.icdc.ebad.repository;

import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringPath;
import fr.icdc.ebad.domain.Directory;
import fr.icdc.ebad.domain.Environnement;
import fr.icdc.ebad.domain.QDirectory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.querydsl.binding.SingleValueBinding;

/**
 * Spring Data JPA repository for the Directory entity.
 */
public interface DirectoryRepository extends JpaRepository<Directory, Long>, QuerydslPredicateExecutor<Directory>, QuerydslBinderCustomizer<QDirectory> {
    @Override
    default void customize(QuerydslBindings bindings, QDirectory root) {
        bindings
                .bind(String.class)
                .first((SingleValueBinding<StringPath, String>) StringExpression::containsIgnoreCase);
    }

    void deleteByEnvironnement(Environnement environnement);
}
