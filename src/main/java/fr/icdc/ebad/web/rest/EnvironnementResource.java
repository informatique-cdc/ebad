package fr.icdc.ebad.web.rest;

import com.querydsl.core.types.Predicate;
import fr.icdc.ebad.domain.Environnement;
import fr.icdc.ebad.service.EnvironnementService;
import fr.icdc.ebad.service.util.EbadServiceException;
import fr.icdc.ebad.web.ResponseUtil;
import fr.icdc.ebad.web.rest.dto.EnvironnementCreationDto;
import fr.icdc.ebad.web.rest.dto.EnvironnementDto;
import fr.icdc.ebad.web.rest.dto.EnvironnementInfoDTO;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/environments")
@Tag(name = "Environment", description = "the environment API")
public class EnvironnementResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnvironnementResource.class);

    private final EnvironnementService environnementService;
    private final MapStructMapper mapStructMapper;

    public EnvironnementResource(EnvironnementService environnementService, MapStructMapper mapStructMapper) {
        this.environnementService = environnementService;
        this.mapStructMapper = mapStructMapper;
    }

    /**
     * GET  /environnements?applicationId={appId} to get environment from application.
     */
    @PreAuthorize("@permissionApplication.canRead(#appId, principal) or @permissionApplication.canManage(#appId, principal)")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PageableAsQueryParam
    public Page<EnvironnementDto> getEnvironmentsFromApp(@RequestParam("applicationId") Long appId, @QuerydslPredicate(root = Environnement.class) Predicate predicate, @Parameter(hidden = true) Pageable pageable) {
        LOGGER.debug("REST request to getEnvironmentsFromApp {}", appId);
        Page<Environnement> environnementPage = environnementService.getEnvironmentFromApp(appId, predicate, PaginationUtil.generatePageRequestOrDefault(pageable));
        return environnementPage.map(mapStructMapper::convert);
    }

    /**
     * GET  /environnement/info/{env} to get info of environnement.
     */
    @PreAuthorize("@permissionEnvironnement.canRead(#env, principal) or @permissionEnvironnement.canWrite(#env, principal)")
    @GetMapping(value = "/info/{env}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public EnvironnementInfoDTO getInfo(@PathVariable Long env) {
        LOGGER.debug("REST request to get info environnement");
        String diskSpace = environnementService.getEspaceDisque(env);
        Date dateTraitement = environnementService.getDateTraiement(env);
        return new EnvironnementInfoDTO(env, diskSpace, dateTraitement);
    }

    /**
     * GET  /environnement/{env} to get one environnement.
     */
    @PreAuthorize("@permissionEnvironnement.canRead(#env, principal) or @permissionEnvironnement.canWrite(#env, principal)")
    @GetMapping(value = "/{env}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<EnvironnementDto> get(@PathVariable Long env) {
        LOGGER.debug("REST request to get one environnement {}", env);
        Optional<EnvironnementDto> environnementDtoOptional = environnementService.findEnvironnement(env)
                .map(mapStructMapper::convert);
        return ResponseUtil.wrapOrNotFound(environnementDtoOptional);
    }

    /**
     * POST  /environnement/info/{env} to change date traitement of environnement.
     */
    @PreAuthorize("@permissionEnvironnement.canRead(#env, principal)")
    @GetMapping(value = "/dateTraitement/{env}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<String> changeDateTraitement(@PathVariable Long env, @RequestParam @DateTimeFormat(pattern = "dd/MM/yyyy") Date dateTraitement) throws EbadServiceException {
        LOGGER.debug("REST request to set new date traitement of environnement");
        environnementService.changeDateTraiement(env, dateTraitement);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    /**
     * PUT  /environnement to add a new environnement
     */
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("@permissionApplication.canWrite(#env.application, principal) && @permissionServiceOpen.canCreateEnvironment() && @permissionIdentity.canRead(#env.identity.id, principal)")
    public ResponseEntity<EnvironnementDto> addEnvironnement(@RequestBody EnvironnementCreationDto env) {
        LOGGER.debug("REST request to add a new environnement {}", env);
        if (env.getPrefix() == null) {
            env.setPrefix("");
        }

        Environnement environnement = environnementService.saveEnvironnement(mapStructMapper.convert(env));
        return new ResponseEntity<>(mapStructMapper.convert(environnement), HttpStatus.OK);
    }

    /**
     * PATCH  /environnement to update an environnement
     */
    @PatchMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("@permissionEnvironnement.canWrite(#env, principal) && @permissionIdentity.canRead(#env.identity.id, principal)")
    public ResponseEntity<EnvironnementDto> updateEnvironnement(@RequestBody EnvironnementDto env) {
        LOGGER.debug("REST request to update an environnement {}", env.getId());
        Environnement result = environnementService.updateEnvironnement(mapStructMapper.convert(env));
        return new ResponseEntity<>(mapStructMapper.convert(result), HttpStatus.OK);
    }

    /**
     * DELETE  /environnement to delete environnement.
     */
    @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("@permissionEnvironnement.canWrite(#idEnv, principal)")
    public ResponseEntity<Void> deleteEnvironnement(@RequestParam Long idEnv) {
        LOGGER.debug("REST request to delete Environnement : {}", idEnv);
        Optional<Environnement> environnementOptional = Optional.ofNullable(environnementService.getEnvironnement(idEnv));
        if (environnementOptional.isPresent()) {
            environnementService.deleteEnvironnement(environnementOptional.get(), false);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping(value = "/import/application/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("@permissionApplication.canWrite(#id, principal)")
    public Set<EnvironnementDto> importEnvApp(@PathVariable Long id) throws EbadServiceException {
        LOGGER.debug("REST request to import all env for app {} ", id);
        return mapStructMapper.convertToEnvironmentDtoSet(environnementService.importEnvironments(id));
    }

    @PostMapping(value = "/import-all", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<EnvironnementDto> importAll() throws EbadServiceException {
        LOGGER.debug("REST request to import all Environments ");
        return mapStructMapper.convertToEnvironmentDtoList(environnementService.importEnvironments());
    }
}
