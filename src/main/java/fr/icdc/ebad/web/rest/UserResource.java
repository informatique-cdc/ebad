package fr.icdc.ebad.web.rest;

import com.querydsl.core.types.Predicate;
import fr.icdc.ebad.domain.User;
import fr.icdc.ebad.service.UserService;
import fr.icdc.ebad.service.util.EbadServiceException;
import fr.icdc.ebad.web.ResponseUtil;
import fr.icdc.ebad.web.rest.dto.AuthorityApplicationDTO;
import fr.icdc.ebad.web.rest.dto.RolesDTO;
import fr.icdc.ebad.web.rest.dto.UserAccountDto;
import fr.icdc.ebad.web.rest.dto.UserDto;
import fr.icdc.ebad.mapper.MapStructMapper;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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
    private final MapStructMapper mapStructMapper;

    public UserResource(UserService userService, MapStructMapper mapStructMapper) {
        this.userService = userService;
        this.mapStructMapper = mapStructMapper;
    }

    @GetMapping("/current")
    @PreAuthorize("isAuthenticated()")
    @Timed
    public ResponseEntity<UserDto> currentUser() throws EbadServiceException {
        User user = this.userService.getUserWithAuthorities();

        return new ResponseEntity<>(mapStructMapper.convert(user), HttpStatus.OK);
    }

    /**
     * GET  /users to get all users.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PageableAsQueryParam
    public Page<UserDto> getAll(@Parameter(hidden = true) Pageable pageable, @QuerydslPredicate(root = User.class) Predicate predicate) {
        LOGGER.debug("REST request to get all Users");
        Page<User> userPage = userService.getAllUsers(predicate, pageable);
        return userPage.map(mapStructMapper::convert);
    }

    /*
    * GET  /users/:login to get the "login" user.
    */
    @GetMapping(value = "/{login}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<UserDto> getUser(@PathVariable String login) {
        LOGGER.debug("REST request to get User : {}", login);
        Optional<UserDto> userDto = userService.getUser(login).map(mapStructMapper::convert);
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
    public ResponseEntity<UserDto> inactivateAccount(@PathVariable String login) throws EbadServiceException {
        Optional<UserDto> userDto = userService.inactivateAccount(login).map(mapStructMapper::convert);
        return ResponseUtil.wrapOrNotFound(userDto);
    }

    /**
     * PUT  /users to save new user.
     */
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("hasRole('ROLE_ADMIN') && @permissionServiceOpen.canCreateOrUpdateUser()")
    public ResponseEntity<UserDto> saveUser(@Valid @RequestBody UserAccountDto userDto) throws EbadServiceException {
        LOGGER.debug("REST request to save new User");
        User user = userService.createUser(userDto.getLogin(), userDto.getEmail(), userDto.getFirstName(), userDto.getLastName(), userDto.getPassword());
        return ResponseEntity.ok(mapStructMapper.convert(user));
    }

    /**
     * PATCH  /users to save  user.
     */
    @PatchMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("hasRole('ROLE_ADMIN') && @permissionServiceOpen.canCreateOrUpdateUser()")
    public ResponseEntity<UserDto> updateUser(@RequestBody UserDto userDto) throws EbadServiceException {
        LOGGER.debug("REST request to save  User");
        User user = userService.updateUser(userDto.getId(), userDto.getLogin(), userDto.getEmail(), userDto.getFirstName(), userDto.getLastName(), userDto.getPassword());
        return new ResponseEntity<>(mapStructMapper.convert(user), HttpStatus.OK);
    }


    /**
     * PATCH  /users/roles to change roles of user.
     */
    @PatchMapping(value = "/roles", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("hasRole('ROLE_ADMIN') && @permissionServiceOpen.canCreateOrUpdateUser()")
    public ResponseEntity<UserDto> changeRoles(@RequestBody RolesDTO roles) throws EbadServiceException {
        LOGGER.debug("REST request to change role User : {}", roles.getLoginUser());
        User user = userService.changeRoles(roles.getLoginUser(), roles.isRoleAdmin(), roles.isRoleUser());
        return ResponseEntity.ok(mapStructMapper.convert(user));
    }

    /**
     * PATCH  /users/application to add or remove application of user.
     */
    @PatchMapping(value = "/application", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("hasRole('ROLE_ADMIN') or @permissionApplication.canManage(#authorityApplicationDTO.idApplication,principal) or @permissionApplication.canWrite(#authorityApplicationDTO.idApplication, principal)")
    public ResponseEntity<UserDto> changeApplicationAuthority(@RequestBody AuthorityApplicationDTO authorityApplicationDTO) {
        LOGGER.debug("REST request to change authorityApplication User : {}", authorityApplicationDTO.getLoginUser());
        User user = userService.changeAutorisationApplication(authorityApplicationDTO);
        if(user == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }else{
            return new ResponseEntity<>(mapStructMapper.convert(user), HttpStatus.OK);
        }
    }
}
