package fr.icdc.ebad.config.apikey;

import fr.icdc.ebad.config.properties.EbadProperties;
import fr.icdc.ebad.security.apikey.ApiKeyAuthenticationManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;

@Configuration
@EnableWebSecurity
@Order(2)
public class ApiSecurityConfig extends WebSecurityConfigurerAdapter {

    private final ApiKeyAuthenticationManager apiKeyAuthenticationManager;
    private final EbadProperties ebadProperties;

    public ApiSecurityConfig(ApiKeyAuthenticationManager apiKeyAuthenticationManager, EbadProperties ebadProperties) {
        this.apiKeyAuthenticationManager = apiKeyAuthenticationManager;
        this.ebadProperties = ebadProperties;
    }

    @Bean
    public RequestHeaderAuthenticationFilter requestHeaderAuthenticationFilter() {
        RequestHeaderAuthenticationFilter requestHeaderAuthenticationFilter =  new RequestHeaderAuthenticationFilter();

        requestHeaderAuthenticationFilter.setPrincipalRequestHeader(ebadProperties.getSecurity().getApiKeyHeaderName());
        requestHeaderAuthenticationFilter.setExceptionIfHeaderMissing(false);
        requestHeaderAuthenticationFilter.setAuthenticationManager(apiKeyAuthenticationManager);

        return requestHeaderAuthenticationFilter;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http
                .requestMatcher(request -> {
                    String auth = request.getHeader(ebadProperties.getSecurity().getApiKeyHeaderName());
                    return (auth != null);
                })
                .csrf().disable()
                .addFilter(requestHeaderAuthenticationFilter())
                .authorizeRequests()
                .anyRequest().authenticated()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        // @formatter:on
    }
}
