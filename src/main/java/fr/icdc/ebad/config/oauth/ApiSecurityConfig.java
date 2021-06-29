package fr.icdc.ebad.config.oauth;

import fr.icdc.ebad.security.Test2Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
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
@Profile("!jwt")
public class ApiSecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private Test2Filter test2Filter;


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
        http
                .csrf().disable()
                .requestMatcher(request -> {
                    String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
                    return (auth != null && auth.startsWith("Basic"));
                })
                .addFilterBefore(test2Filter, BasicAuthenticationFilter.class)
                .httpBasic().disable()
                .authorizeRequests()
                .anyRequest().authenticated()
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        // @formatter:on
    }
}
