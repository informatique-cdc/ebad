package fr.icdc.ebad.web.rest;

import fr.icdc.ebad.domain.Actualite;
import fr.icdc.ebad.security.SecurityUtils;
import fr.icdc.ebad.service.ActualiteService;
import fr.icdc.ebad.web.ResponseUtil;
import fr.icdc.ebad.web.rest.dto.ActualiteDto;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.tags.Tag;
import ma.glasnost.orika.MapperFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/news")
@Tag(name = "New", description = "the new API")
public class ActualiteResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActualiteResource.class);

    private final ActualiteService actualiteService;
    private final MapperFacade mapper;

    public ActualiteResource(ActualiteService actualiteService, MapperFacade mapper) {
        this.actualiteService = actualiteService;
        this.mapper = mapper;
    }

    /**
     * GET  /api/actualites to get all actualites.
     */
    @GetMapping
    @Timed
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<ActualiteDto> getAll() {
        LOGGER.debug("REST request to get all Actualites - Read");
        return mapper.mapAsList(actualiteService.getAllActualites(), ActualiteDto.class);
    }

    /**
     * GET  /api/actualites/public to get all actualites.
     */
    @GetMapping("/public")
    @Timed
    @PreAuthorize("isAuthenticated()")
    public List<ActualiteDto> getActualityPublished() {
        LOGGER.debug("REST request to get all public Actualites - Read");
        return mapper.mapAsList(actualiteService.getAllActualitesPubliees(), ActualiteDto.class);
    }

    /**
     * GET  /api/actualites/:id to get the given actualite
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ActualiteDto> getOne(@PathVariable("id") Long id) {
        LOGGER.debug("REST request to get Actualite with id {}", id);
        Optional<ActualiteDto> actualiteDtoOptional = actualiteService.getActualite(id).map(actualite -> mapper.map(actualite, ActualiteDto.class));
        return ResponseUtil.wrapOrNotFound(actualiteDtoOptional);
    }

    /**
     * PUT  /api/actualites to save a new actualite
     */
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> save(@RequestBody ActualiteDto actualiteDto) {
        LOGGER.debug("REST request to save Actualite");
        if (actualiteDto.getId() != null) {
            return ResponseEntity.badRequest().build();
        }
        actualiteDto.setCreatedBy(SecurityUtils.getCurrentLogin());
        actualiteService.saveActualite(mapper.map(actualiteDto, Actualite.class));
        return ResponseEntity.ok().build();
    }

    /**
     * DELETE  /api/actualites/{id} to delete the given actualite
     */
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        LOGGER.debug("REST request to delete Actualite with id {}", id);
        Actualite actualite = Actualite.builder().id(id).build();
        actualiteService.deleteActualite(actualite);
        return ResponseEntity.ok().build();
    }

    /**
     * PATCH  /api/actualites to update a actualite
     */
    @PatchMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> update(@RequestBody ActualiteDto actualiteDto) {
        LOGGER.debug("REST request to update Actualite");
        if (actualiteDto.getId() == null) {
            return ResponseEntity.badRequest().build();
        }
        actualiteService.saveActualite(mapper.map(actualiteDto, Actualite.class));
        return ResponseEntity.ok().build();
    }
}
