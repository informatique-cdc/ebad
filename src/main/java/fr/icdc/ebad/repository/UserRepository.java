package fr.icdc.ebad.repository;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringPath;
import fr.icdc.ebad.domain.QUser;
import fr.icdc.ebad.domain.User;
import org.joda.time.DateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
 * Spring Data JPA repository for the User entity.
 */
public interface UserRepository extends JpaRepository<User, Long>, QuerydslPredicateExecutor<User>, QuerydslBinderCustomizer<QUser> {
    @Override
    default void customize(QuerydslBindings bindings, QUser root) {
        bindings
                .bind(String.class)
                .first((SingleValueBinding<StringPath, String>) StringExpression::containsIgnoreCase);
    }

    @Override
    Page<User> findAll(Predicate predicate, Pageable pageable);

    @EntityGraph(attributePaths = {"authorities", "usageApplications"})
    @Override
    User save(User user);

    Optional<User> findOneByActivationKey(String activationKey);

    List<User> findAllByActivatedIsFalseAndCreatedDateBefore(DateTime dateTime);

    Optional<User> findOneByEmail(String email);

    @Query("select user from User user  left join fetch user.authorities authorities where user.login = :login")
    @EntityGraph(attributePaths = {"usageApplications", "authorities",
    })
    Optional<User> findOneByLogin(String login);

    @Query("select user from User user left join fetch  user.usageApplications usageApplication left join fetch usageApplication.application application left join fetch user.authorities authorities where user.login = :login")
    Optional<User> findOneByLoginUser(@Param("login") String login);

    @Query("select user from User user left join user.usageApplications usageApplication left join usageApplication.application application left join application.environnements environnement left join environnement.batchs batch where batch.id = :batch and user.login = :login and usageApplication.canUse = true")
    User findUserFromBatch(@Param("batch") Long batch, @Param("login") String login);

    @Query("select user from User user left join user.usageApplications usageApplication left join usageApplication.application application left join application.environnements environnement left join environnement.batchs batch where batch.id = :batch and user.login = :login and usageApplication.canManage = true")
    User findManagerFromBatch(@Param("batch") Long batch, @Param("login") String login);

    @Query("select user from User user left join  user.usageApplications usageApplication on user.id = usageApplication.user left join usageApplication.application application where application.id = :application and user.login = :login and usageApplication.canUse = true")
    User findUserFromApplication(@Param("application") Long application, @Param("login") String login);

    @Query("select user from User user left join  user.usageApplications usageApplication on user.id = usageApplication.user left join usageApplication.application application where application.id = :application and user.login = :login and usageApplication.canManage = true")
    User findManagerFromApplication(@Param("application") Long application, @Param("login") String login);

    @Query("select user from User user left join  user.usageApplications usageApplication left join usageApplication.application application left join application.environnements environnement where environnement.id = :env and user.login = :login and usageApplication.canUse = true")
    User findUserFromEnv(@Param("env") Long env, @Param("login") String login);

    @Query("select user from User user left join  user.usageApplications usageApplication left join usageApplication.application application left join application.environnements environnement where environnement.id = :env and user.login = :login and usageApplication.canManage = true")
    User findManagerFromEnv(@Param("env") Long env, @Param("login") String login);

    @EntityGraph(attributePaths = {"authorities", "usageApplications"})
    Optional<User> findOneById(Long id);
}
