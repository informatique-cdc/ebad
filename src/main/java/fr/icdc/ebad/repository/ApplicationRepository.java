package fr.icdc.ebad.repository;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringPath;
import fr.icdc.ebad.domain.Application;
import fr.icdc.ebad.domain.QApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.querydsl.binding.SingleValueBinding;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the Application entity.
 */
public interface ApplicationRepository extends JpaRepository<Application, Long>, QuerydslPredicateExecutor<Application>, QuerydslBinderCustomizer<QApplication> {
    @Override
    default void customize(QuerydslBindings bindings, QApplication root) {
        bindings
                .bind(String.class)
                .first((SingleValueBinding<StringPath, String>) StringExpression::containsIgnoreCase);
    }

    @EntityGraph(attributePaths = {"environnements", "environnements.batchs",
            "environnements.batchs.chaineAssociations", "environnements.logBatchs", "usageApplications", "environnements.batchs.environnements",
    })
    @Override
    List<Application> findAll(Sort sort);

    @EntityGraph(attributePaths = {"environnements", "environnements.batchs",
            "environnements.batchs.chaineAssociations", "environnements.logBatchs", "usageApplications", "environnements.batchs.environnements",
    })
    @Override
    Page<Application> findAll(Pageable pageable);

    @Override
    Page<Application> findAll(Predicate predicate, Pageable pageable);

    @EntityGraph(attributePaths = {"environnements", "environnements.batchs",
            "environnements.batchs.chaineAssociations", "environnements.logBatchs", "usageApplications",
    })
    @Override
    Optional<Application> findById(Long id);

    @EntityGraph(attributePaths = {"environnements", "environnements.batchs",
            "environnements.batchs.chaineAssociations", "environnements.logBatchs", "usageApplications",
    })
    @Override
    Application save(Application application);

    Optional<Application> findAllByExternalIdAndPluginId(String externalId, String pluginId);


    @Query("select application from Application application" +
            " left join application.usageApplications usageApplications" +
            " left join usageApplications.user user" +
            " where user.login = :login and usageApplications.canManage = true")
    Page<Application> findAllManagedByUser(@Param("login") String login, Pageable pageable);


    @Query("select application from Application application" +
            " left join application.usageApplications usageApplications" +
            " left join usageApplications.user user" +
            " where user.login = :login and usageApplications.canUse = true")
    Page<Application> findAllUsagedByUser(@Param("login") String login, Pageable pageable);
}
