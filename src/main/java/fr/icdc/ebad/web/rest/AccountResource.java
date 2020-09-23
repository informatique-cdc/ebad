package fr.icdc.ebad.web.rest;

import fr.icdc.ebad.domain.Authority;
import fr.icdc.ebad.repository.UserRepository;
import fr.icdc.ebad.security.SecurityUtils;
import fr.icdc.ebad.service.UserService;
import fr.icdc.ebad.service.util.EbadServiceException;
import fr.icdc.ebad.web.rest.dto.UserAccountDto;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * REST controller for managing the current user's account.
 */
@RestController
@Tag(name = "Account", description = "the account API")
public class AccountResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountResource.class);
    private static final int SIZE_MIN_PASSWORD = 5;
    private static final int SIZE_MAX_PASSWORD = 50;

    private final UserRepository userRepository;
    private final UserService userService;


    public AccountResource(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    /**
     * GET  /activate to activate the registered user.
     *
     * @param key key
     * @return account
     */
    @GetMapping(value = "/activate", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<String> activateAccount(@RequestParam(value = "key") String key) {
        return userService.activateRegistration(key).map(user -> new ResponseEntity<String>(HttpStatus.OK)).orElse(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    /**
     * GET  /authenticate to check if the user is authenticated, and return its login.
     *
     * @param request the http request
     * @return user login
     */
    @GetMapping(value = "/authenticate", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public String isAuthenticated(HttpServletRequest request) {
        LOGGER.debug("REST request to check if the current user is authenticated");
        return SecurityUtils.getCurrentLogin();
    }

    /**
     * GET  /account to get the current user.
     *
     * @return current user
     */
    @GetMapping(value = "/account", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<UserAccountDto> getAccount() throws EbadServiceException {
        return Optional.ofNullable(userService.getUserWithAuthorities()).map(user -> new ResponseEntity<>(new UserAccountDto(user.getLogin(), null, user.getFirstName(), user.getLastName(), user.getEmail(), user.getLangKey(), user.getAuthorities().stream().map(Authority::getName).collect(Collectors.toCollection(LinkedList::new))), HttpStatus.OK)).orElse(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @GetMapping(value = "/csrf", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> getCsrf() {
        return ResponseEntity.ok().build();
    }

    /**
     * POST  /account to update the current user information.
     *
     * @param userDTO the new user
     * @return current user
     */
    @PostMapping(value = "/account", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> saveAccount(@RequestBody UserAccountDto userDTO) {
        return userRepository.findOneByLogin(userDTO.getLogin()).filter(u -> u.getLogin().equals(SecurityUtils.getCurrentLogin())).map(u -> {
            userService.updateUserInformation(userDTO.getFirstName(), userDTO.getLastName(), userDTO.getEmail());
            return ResponseEntity.ok();
        }).orElse(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR))
                .build();
    }

    /**
     * POST  /change_password to changes the current user's password
     *
     * @param password the new password
     * @return current user
     */
    @PostMapping(value = "/account/change_password", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> changePassword(@RequestBody String password) {
        if (StringUtils.isEmpty(password) || password.length() < SIZE_MIN_PASSWORD || password.length() > SIZE_MAX_PASSWORD) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        userService.changePassword(password);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
