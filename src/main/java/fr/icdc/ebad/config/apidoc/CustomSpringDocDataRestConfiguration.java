package fr.icdc.ebad.config.apidoc;

import com.querydsl.core.types.Predicate;
import org.springdoc.core.customizers.DataRestDelegatingMethodParameterCustomizer;
import org.springdoc.core.customizers.DelegatingMethodParameterCustomizer;
import org.springdoc.core.providers.RepositoryRestConfigurationProvider;
import org.springdoc.core.providers.SpringDataWebPropertiesProvider;
import org.springdoc.data.rest.customisers.QuerydslPredicateOperationCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.querydsl.binding.QuerydslBindingsFactory;

import java.util.Optional;

import static org.springdoc.core.Constants.SPRINGDOC_ENABLED;
import static org.springdoc.core.SpringDocUtils.getConfig;

/**
 * The type Spring doc data rest configuration.
 * @author bnasslashen
 */
@Lazy(false)
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = SPRINGDOC_ENABLED, matchIfMissing = true)
public class CustomSpringDocDataRestConfiguration {

    /**
     * Delegating method parameter customizer delegating method parameter customizer.
     *
     * @param optionalSpringDataWebPropertiesProvider the optional spring data web properties provider
     * @param optionalRepositoryRestConfiguration the optional repository rest configuration
     * @return the delegating method parameter customizer
     */
    @Bean
    @ConditionalOnMissingBean
    @Lazy(false)
    DelegatingMethodParameterCustomizer delegatingMethodParameterCustomizer(Optional<SpringDataWebPropertiesProvider> optionalSpringDataWebPropertiesProvider, Optional<RepositoryRestConfigurationProvider> optionalRepositoryRestConfiguration) {
        return new DataRestDelegatingMethodParameterCustomizer(optionalSpringDataWebPropertiesProvider, optionalRepositoryRestConfiguration);
    }


    /**
     * The type Querydsl provider.
     * @author bnasslashen
     */
    @ConditionalOnClass(value = { QuerydslBindingsFactory.class })
    class QuerydslProvider {

        /**
         * Query dsl querydsl predicate operation customizer querydsl predicate operation customizer.
         *
         * @param querydslBindingsFactory the querydsl bindings factory
         * @return the querydsl predicate operation customizer
         */
        @Bean
        @ConditionalOnMissingBean
        @Lazy(false)
        QuerydslPredicateOperationCustomizer queryDslQuerydslPredicateOperationCustomizer(Optional<QuerydslBindingsFactory> querydslBindingsFactory) {
            if (querydslBindingsFactory.isPresent()) {
                getConfig().addRequestWrapperToIgnore(Predicate.class);
                return new QuerydslPredicateOperationCustomizer(querydslBindingsFactory.get());
            }
            return null;
        }
    }


}
