package fr.icdc.ebad.web.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.icdc.ebad.domain.User;
import fr.icdc.ebad.security.jwt.JWTConfigurer;
import fr.icdc.ebad.security.jwt.TokenProvider;
import fr.icdc.ebad.service.UserService;
import fr.icdc.ebad.service.util.EbadServiceException;
import fr.icdc.ebad.web.rest.dto.LoginDto;
import fr.icdc.ebad.web.rest.dto.UserDto;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.tags.Tag;
import ma.glasnost.orika.MapperFacade;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * Controller to authenticate users.
 */
@Profile("jwt")
@RestController
@Tag(name = "JWT", description = "the jwt authentication API")
public class UserJWTController {

    private final TokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final MapperFacade mapper;

    public UserJWTController(TokenProvider tokenProvider, AuthenticationManager authenticationManager, UserService userService, MapperFacade mapper) {
        this.tokenProvider = tokenProvider;
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.mapper = mapper;
    }

    @PostMapping("/authenticate")
    @Timed
    public ResponseEntity<UserDto> authorize(@Valid @RequestBody LoginDto loginDto) throws EbadServiceException {

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword());

        Authentication authentication = this.authenticationManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        boolean rememberMe = loginDto.isRememberMe();
        String jwt = tokenProvider.createToken(authentication, rememberMe);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(JWTConfigurer.AUTHORIZATION_HEADER, "Bearer " + jwt);

        User user = userService.getUserWithAuthorities();
        user.setToken(jwt);
        return new ResponseEntity<>(mapper.map(user, UserDto.class), httpHeaders, HttpStatus.OK);
    }


    /**
     * Object to return as body in JWT Authentication.
     */
    static class JWTToken {

        private String idToken;

        JWTToken(String idToken) {
            this.idToken = idToken;
        }

        @JsonProperty("id_token")
        String getIdToken() {
            return idToken;
        }

        void setIdToken(String idToken) {
            this.idToken = idToken;
        }
    }
}
