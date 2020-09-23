package fr.icdc.ebad.repository;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringPath;
import fr.icdc.ebad.domain.Chaine;
import fr.icdc.ebad.domain.Environnement;
import fr.icdc.ebad.domain.QChaine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.querydsl.binding.SingleValueBinding;

/**
 * Spring Data JPA repository for the Chaine entity.
 */
public interface ChaineRepository extends JpaRepository<Chaine, Long>, QuerydslPredicateExecutor<Chaine>, QuerydslBinderCustomizer<QChaine> {
    @Override
    default void customize(QuerydslBindings bindings, QChaine root) {
        bindings
                .bind(String.class)
                .first((SingleValueBinding<StringPath, String>) StringExpression::containsIgnoreCase);
    }

    @EntityGraph(attributePaths = {"chaineAssociations", "environnement"})
    @Override
    Page<Chaine> findAll(Predicate predicate, Pageable pageable);

    void deleteByEnvironnement(Environnement environnement);
}
