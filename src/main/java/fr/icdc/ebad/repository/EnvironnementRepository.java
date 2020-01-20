package fr.icdc.ebad.repository;

import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringPath;
import fr.icdc.ebad.domain.Environnement;
import fr.icdc.ebad.domain.QEnvironnement;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.querydsl.binding.SingleValueBinding;

import java.util.Optional;

/**
 * Spring Data JPA repository for the Environnement entity.
 */
public interface EnvironnementRepository extends JpaRepository<Environnement, Long>, QuerydslPredicateExecutor<Environnement>, QuerydslBinderCustomizer<QEnvironnement> {
    @Override
    default void customize(QuerydslBindings bindings, QEnvironnement root) {
        bindings
                .bind(String.class)
                .first((SingleValueBinding<StringPath, String>) StringExpression::containsIgnoreCase);
    }

    @EntityGraph(attributePaths = {"batchs"})
    @Override
    Environnement save(Environnement environnement);

    Optional<Environnement> findAllByExternalIdAndPluginId(String externalId, String pluginId);

}
