package fr.icdc.ebad.web.rest;

import fr.icdc.ebad.domain.User;
import fr.icdc.ebad.service.UserService;
import fr.icdc.ebad.service.util.EbadServiceException;
import fr.icdc.ebad.web.ResponseUtil;
import fr.icdc.ebad.web.rest.dto.AuthorityApplicationDTO;
import fr.icdc.ebad.web.rest.dto.RolesDTO;
import fr.icdc.ebad.web.rest.dto.UserAccountDto;
import fr.icdc.ebad.web.rest.dto.UserDto;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.tags.Tag;
import ma.glasnost.orika.MapperFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing users.
 */
@RestController
@RequestMapping("/users")
@Tag(name = "User", description = "the user API")
public class UserResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserResource.class);

    private final UserService userService;
    private final MapperFacade mapper;

    public UserResource(UserService userService, MapperFacade mapper) {
        this.userService = userService;
        this.mapper = mapper;
    }

    @GetMapping("/current")
    @PreAuthorize("isAuthenticated()")
    @Timed
    public ResponseEntity<User> currentUser() throws EbadServiceException {
        User user = this.userService.getUserWithAuthorities();
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    /**
     * GET  /users to get all users.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    //TODO dtrouillet mettre en place une pagination
    public List<UserDto> getAll() {
        LOGGER.debug("REST request to get all Users");
        List<User> userList = userService.getAllUsers();
        return mapper.mapAsList(userList, UserDto.class);
    }

    /*
    * GET  /users/:login to get the "login" user.
    */
    @GetMapping(value = "/{login}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserDto> getUser(@PathVariable String login) {
        LOGGER.debug("REST request to get User : {}", login);
        Optional<UserDto> userDto = userService.getUser(login).map(user -> mapper.map(user, UserDto.class));
        return ResponseUtil.wrapOrNotFound(userDto);
    }

    /**
     * GET  /users/:login to get the "login" user.
     */
    @GetMapping(value = "/activate", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> activateAccount(@RequestParam(value = "key") String key) {
        return Optional.ofNullable(userService.activateRegistration(key))
                .map(user -> new ResponseEntity<Void>(HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    /**
     * GET  /users/:login to get the "login" user.
     */
    @GetMapping(value = "/inactivate/{login}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserDto> inactivateAccount(@PathVariable String login) {
        Optional<UserDto> userDto = userService.inactivateAccount(login).map(user -> mapper.map(user, UserDto.class));
        return ResponseUtil.wrapOrNotFound(userDto);
    }

    /**
     * PUT  /users to save new user.
     */
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserDto> saveUser(@Valid @RequestBody UserAccountDto userDto) throws EbadServiceException {
        LOGGER.debug("REST request to save new User");
        User user = userService.createUser(userDto.getLogin(), userDto.getEmail(), userDto.getFirstName(), userDto.getLastName(), userDto.getPassword());
        return ResponseEntity.ok(mapper.map(user, UserDto.class));
    }

    /**
     * PATCH  /users to save  user.
     */
    @PatchMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserDto> updateUser(@RequestBody UserDto userDto) throws EbadServiceException {
        LOGGER.debug("REST request to save  User");
        User user = userService.updateUser(userDto.getId(), userDto.getLogin(), userDto.getEmail(), userDto.getFirstName(), userDto.getLastName(), userDto.getPassword());
        return new ResponseEntity<>(mapper.map(user, UserDto.class), HttpStatus.OK);
    }


    /**
     * PATCH  /users/roles to change roles of user.
     */
    @PatchMapping(value = "/roles", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserDto> changeRoles(@RequestBody RolesDTO roles) throws EbadServiceException {
        LOGGER.debug("REST request to change role User : {}", roles.getLoginUser());
        User user = userService.changeRoles(roles.getLoginUser(), roles.isRoleAdmin(), roles.isRoleUser());
        return ResponseEntity.ok(mapper.map(user, UserDto.class));
    }

    /**
     * PATCH  /users/application to add or remove application of user.
     */
    @PatchMapping(value = "/application", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserDto> changeApplicationAuthority(@RequestBody AuthorityApplicationDTO authorityApplicationDTO) {
        LOGGER.debug("REST request to change authorityApplication User : {}", authorityApplicationDTO.getLoginUser());
        User user = userService.changeAutorisationApplication(authorityApplicationDTO);
        if(user == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }else{
            return new ResponseEntity<>(mapper.map(user, UserDto.class), HttpStatus.OK);
        }
    }
}
