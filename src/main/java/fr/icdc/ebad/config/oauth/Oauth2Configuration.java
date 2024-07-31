package fr.icdc.ebad.config.oauth;

import fr.icdc.ebad.config.properties.EbadProperties;
import fr.icdc.ebad.repository.AuthorityRepository;
import fr.icdc.ebad.repository.UserRepository;
import fr.icdc.ebad.security.EbadUserDetailsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Profile("!jwt")
@Order(3)
@Configuration
//@EnableOAuth2Client
//@EnableResourceServer
//@Import(SecurityProblemSupport.class)
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class Oauth2Configuration {

//    private final OauthJwtAuthConverter jwtAuthConverter;
    private final EbadUserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;
    private final EbadProperties ebadProperties;

    public Oauth2Configuration(EbadUserDetailsService userDetailsService, UserRepository userRepository, AuthorityRepository authorityRepository, EbadProperties ebadProperties) {
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
        this.authorityRepository = authorityRepository;
        this.ebadProperties = ebadProperties;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

//    @Override
//    public void configure(ResourceServerSecurityConfigurer resources) {
//        resources.tokenServices(this.resourceServerTokenServices());
//    }

//    @Override
    @Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName(null);

        CookieCsrfTokenRepository cookieCsrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        cookieCsrfTokenRepository.setCookiePath("/");
        cookieCsrfTokenRepository.setHeaderName("X-XSRF-TOKEN");
        cookieCsrfTokenRepository.setCookieName("XSRF-TOKEN");
        cookieCsrfTokenRepository.setCookieHttpOnly(false);

        http
                .exceptionHandling()
//                .authenticationEntryPoint(problemSupport)
//                .accessDeniedHandler(problemSupport)
                .and().csrf().csrfTokenRepository(cookieCsrfTokenRepository)
                .csrfTokenRequestHandler(requestHandler)
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/csrf").permitAll()
                        .requestMatchers("/news/public").permitAll()
                        .requestMatchers("/ws").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/v3/api-docs**").permitAll()
                        .requestMatchers("/swagger-resources/configuration/ui").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt().jwtAuthenticationConverter(jwtAuthenticationConverter()))
                .cors();
        return http.build();

    }




//    @Bean
//    public PrincipalExtractor ebadPrincipalExtractor() {
//        return new EbadPrincipalExtractor(userDetailsService, userRepository, authorityRepository, ebadProperties);
//    }
//
//    @Bean
//    public AuthoritiesExtractor ebadAuthoritiesExtractor() {
//        return new EbadAuthoritiesExtractor(ebadProperties);
//    }
//
//    @Bean
//    ResourceServerTokenServices resourceServerTokenServices() {
//        UserInfoTokenServices userInfoTokenServices = new UserInfoTokenServices(resourceServerProperties.getUserInfoUri(), resourceServerProperties.getClientId());
//        userInfoTokenServices.setAuthoritiesExtractor(ebadAuthoritiesExtractor());
//        userInfoTokenServices.setPrincipalExtractor(ebadPrincipalExtractor());
//        return userInfoTokenServices;
//    }

    @Bean
    public Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
        return new CustomJwtAuthenticationConverter(userDetailsService, userRepository, authorityRepository, ebadProperties);
    }


}
