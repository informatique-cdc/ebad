package fr.icdc.ebad.web.rest;

import fr.icdc.ebad.domain.Application;
import fr.icdc.ebad.security.SecurityUtils;
import fr.icdc.ebad.service.ApplicationService;
import fr.icdc.ebad.service.util.EbadServiceException;
import fr.icdc.ebad.web.rest.dto.ApplicationDto;
import fr.icdc.ebad.web.rest.dto.UserSimpleDto;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.tags.Tag;
import ma.glasnost.orika.MapperFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/applications")
@Tag(name = "Application", description = "the application API")
public class ApplicationResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationResource.class);

    private final ApplicationService applicationService;
    private final MapperFacade mapper;

    public ApplicationResource(ApplicationService applicationService, MapperFacade mapper) {
        this.applicationService = applicationService;
        this.mapper = mapper;
    }

    /**
     * GET  /application to get all applications.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN', 'ROLE_MODO')")
    @PostFilter("@permissionApplication.canRead(filterObject,principal)")
    public List<ApplicationDto> getAll() {
        LOGGER.debug("REST request to get all Application - Read");
        return mapper.mapAsList(applicationService.getAllApplications(), ApplicationDto.class);
    }

    @PostMapping(value = "/import-all", produces = MediaType.TEXT_PLAIN_VALUE)
    @Timed
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public String importAll() {
        LOGGER.debug("REST request to import all Application ");
        return applicationService.importApp();
    }

    /**
     * GET  /application to get all applications.
     */
    @GetMapping(value = "/write", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN', 'ROLE_MODO')")
    @PostFilter("@permissionApplication.canWrite(filterObject,principal)")
    public List<ApplicationDto> getAllWrite() {
        LOGGER.debug("REST request to get all Application - Write");
        return mapper.mapAsList(applicationService.getAllApplications(), ApplicationDto.class);
    }

    /**
     * GET  /application to get all applications.
     */
    @GetMapping(value = "/gestion", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN', 'ROLE_MODO')")
    @PostFilter("@permissionApplication.canManage(filterObject,principal)")
    public List<ApplicationDto> getAllManage() {
        LOGGER.debug("REST request to get all Application - Write");
        return mapper.mapAsList(applicationService.getAllApplications(), ApplicationDto.class);

    }

    /**
     * PUT  /application to create new application.
     */
    @PutMapping(value = "/gestion", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("@permissionApplication.canManage(#applicationDto,principal)")
    public ApplicationDto createApplication(@RequestBody ApplicationDto applicationDto) {
        LOGGER.debug("REST request to create Application");
        applicationDto.setCreatedBy(SecurityUtils.getCurrentLogin());
        Application application = applicationService.saveApplication(mapper.map(applicationDto, Application.class));
        return mapper.map(application, ApplicationDto.class);
    }

    /**
     * PATCH  /application/gestion to update application.
     */
    @PatchMapping(value = "/gestion", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("@permissionApplication.canManage(#applicationDto,principal)")
    public ApplicationDto updateApplication(@RequestBody ApplicationDto applicationDto) throws EbadServiceException {
        LOGGER.debug("REST request to update Application");
        Application application = mapper.map(applicationDto, Application.class);
        Application applicationSaved = applicationService.updateApplication(application);
        return mapper.map(applicationSaved, ApplicationDto.class);
    }

    /**
     * DELETE  /application/gestion to delete application.
     */
    @DeleteMapping(value = "/gestion", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("@permissionApplication.canManage(#appId,principal)")
    public ResponseEntity<Void> removeApplication(@RequestParam Long appId) {
        LOGGER.debug("REST request to remove Application");
        applicationService.deleteApplication(appId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * GET  /application/users to get all users from applications.
     */
    @GetMapping(value = "/users/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("@permissionApplication.canManage(#id,principal)")
    public Set<UserSimpleDto> getUsersFromApplication(@PathVariable Long id) {
        LOGGER.debug("REST request to get all users from Application");
        return mapper.mapAsSet(applicationService.getUsers(id), UserSimpleDto.class);
    }

    /**
     * GET  /application/moderators to get all moderators from applications.
     */
    @GetMapping(value = "/moderators/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("@permissionApplication.canManage(#id,principal)")
    public Set<UserSimpleDto> getModeratorsFromApplication(@PathVariable Long id) {
        LOGGER.debug("REST request to get all users from Application");
        return mapper.mapAsSet(applicationService.getManagers(id), UserSimpleDto.class);
    }
}
