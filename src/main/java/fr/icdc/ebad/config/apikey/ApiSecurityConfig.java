package fr.icdc.ebad.config.apikey;

import fr.icdc.ebad.security.ApiKeyFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
@Order(2)
public class ApiSecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private ApiKeyFilter apiKeyFilter;


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http
                .csrf().disable()
                .requestMatcher(request -> {
                    String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
                    return (auth != null && auth.startsWith("Basic"));
                })
                .addFilterBefore(apiKeyFilter, BasicAuthenticationFilter.class)
                .httpBasic().disable()
                .authorizeRequests()
                .anyRequest().authenticated()
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        // @formatter:on
    }
}
