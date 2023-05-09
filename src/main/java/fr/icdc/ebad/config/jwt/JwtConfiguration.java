package fr.icdc.ebad.config.jwt;

import fr.icdc.ebad.config.Constants;
import fr.icdc.ebad.security.jwt.JWTConfigurer;
import fr.icdc.ebad.security.jwt.TokenProvider;
import org.apache.commons.compress.utils.Lists;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;


@Profile(Constants.SPRING_PROFILE_JWT)
@Configuration
//@Import(SecurityProblemSupport.class)
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@Order(3)
public class JwtConfiguration  {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final UserDetailsService userDetailsService;
    private final TokenProvider tokenProvider;
//    private final SecurityProblemSupport problemSupport;
    private final Environment environment;
    private final PasswordEncoder passwordEncoder;

    public JwtConfiguration(AuthenticationManagerBuilder authenticationManagerBuilder, UserDetailsService userDetailsService, TokenProvider tokenProvider,
//                            SecurityProblemSupport problemSupport,
                            Environment environment, PasswordEncoder passwordEncoder) {
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.userDetailsService = userDetailsService;
        this.tokenProvider = tokenProvider;
//        this.problemSupport = problemSupport;
        this.environment = environment;
        this.passwordEncoder = passwordEncoder;
    }

//    @PostConstruct
//    public void init() {
//        try {
//            authenticationManagerBuilder
//                    .userDetailsService(userDetailsService)
//                    .passwordEncoder(passwordEncoder);
//        } catch (Exception e) {
//            throw new BeanInitializationException("Security configuration failed", e);
//        }
//    }




//    @Bean
//    public AuthenticationManager customAuthenticationManager() throws Exception {
//        return new AuthenticationManager();
//    }

    @Bean
    public AuthenticationManager customAuthenticationManager(HttpSecurity http) throws Exception {
        // Configure AuthenticationManagerBuilder
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
        // Get AuthenticationManager
        return authenticationManagerBuilder.build();
    }

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        CookieCsrfTokenRepository cookieCsrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        cookieCsrfTokenRepository.setCookiePath("/");
        cookieCsrfTokenRepository.setHeaderName("X-XSRF-TOKEN");
        cookieCsrfTokenRepository.setCookieName("XSRF-TOKEN");
        cookieCsrfTokenRepository.setCookieHttpOnly(false);
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();



        http
                .exceptionHandling()
//                .authenticationEntryPoint(problemSupport)
//                .accessDeniedHandler(problemSupport)
                .and()
                .csrf()
                .csrfTokenRepository(cookieCsrfTokenRepository)
                .csrfTokenRequestHandler(requestHandler)
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeHttpRequests()
                .requestMatchers("/register").permitAll()
                .requestMatchers("/ws").permitAll()
                .requestMatchers("/news/public").permitAll()
                .requestMatchers("/activate").permitAll()
                .requestMatchers("/authenticate").permitAll()
                .requestMatchers("/account/reset-password/init").permitAll()
                .requestMatchers("/account/reset-password/finish").permitAll()
                .requestMatchers("/csrf").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/swagger-ui.html").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/v3/api-docs**").permitAll()
                .requestMatchers("/swagger-resources/configuration/ui").permitAll()
                .requestMatchers("/swagger-ui/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .cors()
                .and()
//                .addFilter(new AuthorizationFilter(authenticationManager, userRepository))
                .authenticationManager(customAuthenticationManager(http))
                .apply(securityConfigurerAdapter());

        if(Arrays.stream(environment.getActiveProfiles()).anyMatch("disable-csrf"::equalsIgnoreCase)){
            http.csrf().disable();
        }

        return http.build();
    }


    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        String[] sites = {"http://localhost:4200", "http://localhost:63342", "https://ebad-front.herokuapp.com"};
        String[] methods = {"GET", "POST", "OPTIONS", "HEAD", "PUT", "PATCH", "DELETE"};
        configuration.setAllowedOrigins(Lists.newArrayList(Arrays.stream(sites).iterator()));
        configuration.setAllowedMethods(Lists.newArrayList(Arrays.stream(methods).iterator()));
        configuration.setAllowCredentials(true);
        configuration.addAllowedHeader("*");
        configuration.setMaxAge(10L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private JWTConfigurer securityConfigurerAdapter() {
        return new JWTConfigurer(tokenProvider);
    }

}
