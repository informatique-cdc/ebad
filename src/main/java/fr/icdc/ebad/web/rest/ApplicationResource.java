package fr.icdc.ebad.web.rest;

import com.querydsl.core.types.Predicate;
import fr.icdc.ebad.domain.Application;
import fr.icdc.ebad.security.SecurityUtils;
import fr.icdc.ebad.service.ApplicationService;
import fr.icdc.ebad.service.util.EbadServiceException;
import fr.icdc.ebad.web.rest.dto.ApplicationDto;
import fr.icdc.ebad.web.rest.dto.ApplicationSimpleDto;
import fr.icdc.ebad.web.rest.dto.UsageApplicationDto;
import fr.icdc.ebad.web.rest.dto.UserSimpleDto;
import fr.icdc.ebad.web.rest.util.PaginationUtil;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.tags.Tag;
import ma.glasnost.orika.MapperFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

import java.security.Principal;
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

    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public Page<ApplicationSimpleDto> findApplication(Pageable pageable, @QuerydslPredicate(root = Application.class) Predicate predicate) {
        LOGGER.debug("REST request to find Application - Read");
        return applicationService.findApplication(predicate, PaginationUtil.generatePageRequestOrDefault(pageable))
                .map(application -> mapper.map(application, ApplicationSimpleDto.class));
    }

    /**
     * GET  /application to get all applications.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public Page<ApplicationDto> getAll(Pageable pageable, Principal principal) {
        LOGGER.debug("REST request to get all Application - Read");
        return applicationService.getAllApplicationsUsed(PaginationUtil.generatePageRequestOrDefault(pageable), principal.getName())
                .map(application -> mapper.map(application, ApplicationDto.class));
    }

    @PostMapping(value = "/import-all", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> importAll() {
        LOGGER.debug("REST request to import all Application ");
        applicationService.importApp();
        return ResponseEntity.ok().build();
    }

    /**
     * GET  /application to get all applications.
     */
    @GetMapping(value = "/write", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public Page<ApplicationDto> getAllWrite(Pageable pageable, Principal principal) {
        LOGGER.debug("REST request to get all Application - Write");
        return applicationService.getAllApplicationsManaged(
                PaginationUtil.generatePageRequestOrDefault(pageable), principal.getName())
                .map(application -> mapper.map(application, ApplicationDto.class));
    }

    /**
     * GET  /application to get all applications.
     */
    @GetMapping(value = "/gestion", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Page<ApplicationDto> getAllManage(Pageable pageable, @QuerydslPredicate(root = Application.class) Predicate predicate) {
        LOGGER.debug("REST request to get all Application - Write");
        return applicationService.getAllApplications(predicate, pageable)
                .map(application -> mapper.map(application, ApplicationDto.class));
    }

    /**
     * PUT  /application to create new application.
     */
    @PutMapping(value = "/gestion", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("@permissionApplication.canManage(#applicationDto,principal) && @permissionServiceOpen.canCreateApplication()")
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
    @PreAuthorize("@permissionApplication.canManage(#id,principal) or @permissionApplication.canWrite(#id, principal)")
    public Set<UserSimpleDto> getUsersFromApplication(@PathVariable Long id) {
        LOGGER.debug("REST request to get all users from Application");
        return mapper.mapAsSet(applicationService.getUsers(id), UserSimpleDto.class);
    }

    /**
     * GET  /application/moderators to get all moderators from applications.
     */
    @GetMapping(value = "/moderators/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("@permissionApplication.canManage(#id,principal) or @permissionApplication.canWrite(#id, principal)")
    public Set<UserSimpleDto> getModeratorsFromApplication(@PathVariable Long id) {
        LOGGER.debug("REST request to get all users from Application");
        return mapper.mapAsSet(applicationService.getManagers(id), UserSimpleDto.class);
    }

    @GetMapping(value = "/{id}/usages")
    @PreAuthorize("@permissionApplication.canManage(#id,principal) or @permissionApplication.canWrite(#id, principal)")
    public Page<UsageApplicationDto> getAllUsages(@PathVariable Long id, Pageable pageable) {
        LOGGER.debug("REST request to get all usages from Application");
        return applicationService.getUsage(pageable, id).map(usage -> mapper.map(usage, UsageApplicationDto.class));
    }
}
