package fr.icdc.ebad.web.rest;

import fr.icdc.ebad.domain.Identity;
import fr.icdc.ebad.service.IdentityService;
import fr.icdc.ebad.web.rest.dto.CompleteIdentityDto;
import fr.icdc.ebad.web.rest.dto.PublicIdentityDto;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.tags.Tag;
import ma.glasnost.orika.MapperFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/identities")
@Tag(name = "Identity", description = "the identity API")
public class IdentityResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdentityResource.class);

    private final IdentityService identityService;
    private final MapperFacade mapper;

    public IdentityResource(IdentityService identityService, MapperFacade mapper) {
        this.identityService = identityService;
        this.mapper = mapper;
    }

    /**
     * PUT  /identities to add a new identity
     */
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<CompleteIdentityDto> addIdentity(@RequestBody @Valid CompleteIdentityDto identityDto) {
        LOGGER.debug("REST request to add a new identity");
        Identity identity = identityService.saveIdentity(mapper.map(identityDto, Identity.class));
        return new ResponseEntity<>(mapper.map(identity, CompleteIdentityDto.class), HttpStatus.OK);
    }

    /**
     * GET  /identities/:id to get an identity
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<CompleteIdentityDto> getOneIdentity(@PathVariable Long id) {
        LOGGER.debug("REST request to get an identity");
        Identity identity = identityService.getIdentity(id);
        return new ResponseEntity<>(mapper.map(identity, CompleteIdentityDto.class), HttpStatus.OK);
    }

    /**
     * GET  /identities to get all identities
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<List<PublicIdentityDto>> getAllIdentities() {
        LOGGER.debug("REST request to get all identities");
        List<Identity> identities = identityService.findAll();
        return new ResponseEntity<>(mapper.mapAsList(identities, PublicIdentityDto.class), HttpStatus.OK);
    }

    /**
     * PATCH  /identities to update an identity
     */
    @PatchMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<CompleteIdentityDto> updateIdentity(@RequestBody CompleteIdentityDto identityDto) {
        LOGGER.debug("REST request to update an identity");
        Identity identity = identityService.saveIdentity(mapper.map(identityDto, Identity.class));
        return new ResponseEntity<>(mapper.map(identity, CompleteIdentityDto.class), HttpStatus.OK);
    }

    /**
     * DELETE  /identities/id to delete an identity
     */
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteIdentity(@PathVariable Long id) {
        LOGGER.debug("REST request to delete an identity");
        identityService.deleteIdentity(id);
        return ResponseEntity.ok().build();
    }
}
