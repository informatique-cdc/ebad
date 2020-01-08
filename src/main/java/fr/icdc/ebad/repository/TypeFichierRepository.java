package fr.icdc.ebad.repository;

import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringPath;
import fr.icdc.ebad.domain.Application;
import fr.icdc.ebad.domain.QTypeFichier;
import fr.icdc.ebad.domain.TypeFichier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.querydsl.binding.SingleValueBinding;

/**
 * Spring Data JPA repository for the TypeFichier entity.
 */
public interface TypeFichierRepository extends JpaRepository<TypeFichier, Long>, QuerydslPredicateExecutor<TypeFichier>, QuerydslBinderCustomizer<QTypeFichier> {
    @Override
    default void customize(QuerydslBindings bindings, QTypeFichier root) {
        bindings
                .bind(String.class)
                .first((SingleValueBinding<StringPath, String>) StringExpression::containsIgnoreCase);
    }
//    @Query("select typeFichier from TypeFichier typeFichier left join typeFichier.application application where application.id = :application")
//    Page<TypeFichier> findTypeFichierFromApplication(PrePageable pageable, @Param("application") Long application);

    void deleteByApplication(Application application);
}
