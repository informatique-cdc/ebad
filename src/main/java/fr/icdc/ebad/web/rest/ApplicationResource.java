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
import fr.icdc.ebad.mapper.MapStructMapper;
import fr.icdc.ebad.web.rest.util.PaginationUtil;
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

import java.security.Principal;
import java.util.Set;

@RestController
@RequestMapping("/applications")
@Tag(name = "Application", description = "the application API")
public class ApplicationResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationResource.class);

    private final ApplicationService applicationService;
    private final MapStructMapper mapStructMapper;

    public ApplicationResource(ApplicationService applicationService, MapStructMapper mapStructMapper) {
        this.applicationService = applicationService;
        this.mapStructMapper = mapStructMapper;
    }

    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    @PageableAsQueryParam
    public Page<ApplicationSimpleDto> findApplication(@Parameter(hidden = true) Pageable pageable, @QuerydslPredicate(root = Application.class) Predicate predicate) {
        LOGGER.debug("REST request to find Application - Read");
        return applicationService.findApplication(predicate, PaginationUtil.generatePageRequestOrDefault(pageable))
                .map(mapStructMapper::convertToApplicationSimpleDto);
    }

    /**
     * GET  /application to get all applications.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    @PageableAsQueryParam
    public Page<ApplicationDto> getAll(@Parameter(hidden = true) Pageable pageable, Principal principal) {
        LOGGER.debug("REST request to get all Application - Read");
        return applicationService.getAllApplicationsUsed(PaginationUtil.generatePageRequestOrDefault(pageable), principal.getName())
                .map(mapStructMapper::convertToApplicationDto);
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
    @PageableAsQueryParam
    public Page<ApplicationDto> getAllWrite(@Parameter(hidden = true) Pageable pageable, Principal principal) {
        LOGGER.debug("REST request to get all Application - Write");
        return applicationService.getAllApplicationsManaged(
                PaginationUtil.generatePageRequestOrDefault(pageable), principal.getName())
                .map(mapStructMapper::convertToApplicationDto);
    }

    /**
     * GET  /application to get all applications.
     */
    @GetMapping(value = "/gestion", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PageableAsQueryParam
    public Page<ApplicationDto> getAllManage(@Parameter(hidden = true) Pageable pageable, @QuerydslPredicate(root = Application.class) Predicate predicate) {
        LOGGER.debug("REST request to get all Application - Write");
        return applicationService.getAllApplications(predicate, pageable)
                .map(mapStructMapper::convertToApplicationDto);
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
        Application application = applicationService.saveApplication(mapStructMapper.convert(applicationDto));
        return mapStructMapper.convertToApplicationDto(application);
    }

    /**
     * PATCH  /application/gestion to update application.
     */
    @PatchMapping(value = "/gestion", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("@permissionApplication.canManage(#applicationDto,principal)")
    public ApplicationDto updateApplication(@RequestBody ApplicationDto applicationDto) throws EbadServiceException {
        LOGGER.debug("REST request to update Application");
        Application application = mapStructMapper.convert(applicationDto);
        Application applicationSaved = applicationService.updateApplication(application);
        return mapStructMapper.convertToApplicationDto(applicationSaved);
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
        return mapStructMapper.convertUserSimpleDtoSet(applicationService.getUsers(id));
    }

    /**
     * GET  /application/moderators to get all moderators from applications.
     */
    @GetMapping(value = "/moderators/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("@permissionApplication.canManage(#id,principal) or @permissionApplication.canWrite(#id, principal)")
    public Set<UserSimpleDto> getModeratorsFromApplication(@PathVariable Long id) {
        LOGGER.debug("REST request to get all users from Application");
        return mapStructMapper.convertUserSimpleDtoSet(applicationService.getManagers(id));
    }

    @GetMapping(value = "/{id}/usages")
    @PreAuthorize("@permissionApplication.canManage(#id,principal) or @permissionApplication.canWrite(#id, principal)")
    @PageableAsQueryParam
    public Page<UsageApplicationDto> getAllUsages(@PathVariable Long id, @Parameter(hidden = true) Pageable pageable) {
        LOGGER.debug("REST request to get all usages from Application");
        return applicationService.getUsage(pageable, id).map(mapStructMapper::convert);
    }
}
