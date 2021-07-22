package fr.icdc.ebad.web.rest;

import fr.icdc.ebad.domain.Actualite;
import fr.icdc.ebad.security.SecurityUtils;
import fr.icdc.ebad.service.NewService;
import fr.icdc.ebad.web.ResponseUtil;
import fr.icdc.ebad.web.rest.dto.ActualiteDto;
import fr.icdc.ebad.web.rest.util.PaginationUtil;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import ma.glasnost.orika.MapperFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.api.annotations.ParameterObject;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

import java.util.Optional;

@RestController
@RequestMapping("/news")
@Tag(name = "New", description = "the new API")
public class NewResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(NewResource.class);

    private final NewService newService;
    private final MapperFacade mapper;

    public NewResource(NewService newService, MapperFacade mapper) {
        this.newService = newService;
        this.mapper = mapper;
    }

    /**
     * GET  /api/actualites to get all actualites.
     */
    @GetMapping
    @Timed
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PageableAsQueryParam
    public Page<ActualiteDto> getAll(@Parameter(hidden = true) Pageable pageable) {
        LOGGER.debug("REST request to get all Actualites - Read");
        Page<Actualite> actualitePage = newService.getAllActualites(PaginationUtil.generatePageRequestOrDefault(pageable));
        return actualitePage.map(theNew -> mapper.map(theNew, ActualiteDto.class));
    }

    /**
     * GET  /api/actualites/public to get all actualites.
     */
    @GetMapping("/public")
    @Timed
    @PreAuthorize("isAuthenticated()")
    @PageableAsQueryParam
    public Page<ActualiteDto> getActualityPublished(@Parameter(hidden = true) Pageable pageable) {
        LOGGER.debug("REST request to get all public Actualites - Read");
        Page<Actualite> actualitePage = newService.getAllActualitesPubliees(PaginationUtil.generatePageRequestOrDefault(pageable));
        return actualitePage.map(theNew -> mapper.map(theNew, ActualiteDto.class));
    }

    /**
     * GET  /api/actualites/:id to get the given actualite
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ActualiteDto> getOne(@PathVariable("id") Long id) {
        LOGGER.debug("REST request to get Actualite with id {}", id);
        Optional<ActualiteDto> actualiteDtoOptional = newService.getActualite(id).map(actualite -> mapper.map(actualite, ActualiteDto.class));
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
        newService.saveActualite(mapper.map(actualiteDto, Actualite.class));
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
        newService.deleteActualite(actualite);
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
        newService.saveActualite(mapper.map(actualiteDto, Actualite.class));
        return ResponseEntity.ok().build();
    }
}
