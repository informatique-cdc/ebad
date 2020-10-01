package fr.icdc.ebad.config.oauth;

import com.google.common.collect.Lists;
import fr.icdc.ebad.config.properties.EbadProperties;
import fr.icdc.ebad.repository.AuthorityRepository;
import fr.icdc.ebad.repository.UserRepository;
import fr.icdc.ebad.security.EbadUserDetailsService;
import org.springframework.boot.autoconfigure.security.oauth2.resource.AuthoritiesExtractor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.PrincipalExtractor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.zalando.problem.spring.web.advice.security.SecurityProblemSupport;

@Profile("!jwt")
@Configuration
@EnableOAuth2Client
@EnableResourceServer
@Import(SecurityProblemSupport.class)
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class Oauth2Configuration extends ResourceServerConfigurerAdapter {
    private final ResourceServerProperties resourceServerProperties;
    private final EbadUserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final SecurityProblemSupport problemSupport;
    private final AuthorityRepository authorityRepository;
    private final EbadProperties ebadProperties;

    public Oauth2Configuration(ResourceServerProperties resourceServerProperties, UserRepository userRepository, SecurityProblemSupport problemSupport, AuthorityRepository authorityRepository, EbadUserDetailsService userDetailsService, EbadProperties ebadProperties) {
        this.resourceServerProperties = resourceServerProperties;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
        this.problemSupport = problemSupport;
        this.authorityRepository = authorityRepository;
        this.ebadProperties = ebadProperties;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) {
        resources.tokenServices(this.resourceServerTokenServices());
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        CookieCsrfTokenRepository cookieCsrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        cookieCsrfTokenRepository.setCookiePath("/");
        cookieCsrfTokenRepository.setHeaderName("X-XSRF-TOKEN");
        cookieCsrfTokenRepository.setCookieName("XSRF-TOKEN");
        cookieCsrfTokenRepository.setCookieHttpOnly(false);

        http
                .exceptionHandling()
                .authenticationEntryPoint(problemSupport)
                .accessDeniedHandler(problemSupport)
                .and().csrf().csrfTokenRepository(cookieCsrfTokenRepository)
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers("/csrf").permitAll()
                .antMatchers("/news/public").permitAll()
                .antMatchers("/actuator/**").permitAll()
                .antMatchers("/swagger-ui.html").permitAll()
                .antMatchers("/v3/api-docs/**").permitAll()
                .antMatchers("/v3/api-docs**").permitAll()
                .antMatchers("/swagger-resources/configuration/ui").permitAll()
                .antMatchers("/swagger-ui/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .cors();

    }

    @Bean
    public PrincipalExtractor ebadPrincipalExtractor() {
        return new EbadPrincipalExtractor(userDetailsService, userRepository, authorityRepository, ebadProperties);
    }

    @Bean
    public AuthoritiesExtractor ebadAuthoritiesExtractor() {
        return new EbadAuthoritiesExtractor(ebadProperties);
    }

    @Bean
    ResourceServerTokenServices resourceServerTokenServices() {
        UserInfoTokenServices userInfoTokenServices = new UserInfoTokenServices(resourceServerProperties.getUserInfoUri(), resourceServerProperties.getClientId());
        userInfoTokenServices.setAuthoritiesExtractor(ebadAuthoritiesExtractor());
        userInfoTokenServices.setPrincipalExtractor(ebadPrincipalExtractor());
        return userInfoTokenServices;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Lists.newArrayList("http://localhost:4200"));
        configuration.setAllowedMethods(Lists.newArrayList("GET", "POST", "OPTIONS", "HEAD", "PUT", "PATCH", "DELETE"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(Lists.newArrayList("x-xsrf-token", "XSRF-TOKEN"));
        configuration.setMaxAge(10L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
