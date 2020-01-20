package fr.icdc.ebad.web.rest;

import com.querydsl.core.types.Predicate;
import fr.icdc.ebad.domain.Environnement;
import fr.icdc.ebad.service.EnvironnementService;
import fr.icdc.ebad.service.util.EbadServiceException;
import fr.icdc.ebad.web.ResponseUtil;
import fr.icdc.ebad.web.rest.dto.EnvironnementCreationDto;
import fr.icdc.ebad.web.rest.dto.EnvironnementDto;
import fr.icdc.ebad.web.rest.dto.EnvironnementInfoDTO;
import fr.icdc.ebad.web.rest.util.PaginationUtil;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.tags.Tag;
import ma.glasnost.orika.MapperFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/environments")
@Tag(name = "Environment", description = "the environment API")
public class EnvironnementResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnvironnementResource.class);

    private final EnvironnementService environnementService;
    private final MapperFacade mapper;

    public EnvironnementResource(EnvironnementService environnementService, MapperFacade mapper) {
        this.environnementService = environnementService;
        this.mapper = mapper;
    }

    /**
     * GET  /environnements?applicationId={appId} to get environment from application.
     */
    @PreAuthorize("@permissionApplication.canRead(#appId, principal) or @permissionApplication.canManage(#appId, principal)")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public Page<EnvironnementDto> getEnvironmentsFromApp(@RequestParam("applicationId") Long appId, @QuerydslPredicate(root = Environnement.class) Predicate predicate, Pageable pageable) {
        LOGGER.debug("REST request to getEnvironmentsFromApp {}", appId);
        Page<Environnement> environnementPage = environnementService.getEnvironmentFromApp(appId, predicate, PaginationUtil.generatePageRequestOrDefault(pageable));
        return environnementPage.map(env -> mapper.map(env, EnvironnementDto.class));
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
     * GET  /environnement/{env} to get oneenvironnement.
     */
    @PreAuthorize("@permissionEnvironnement.canRead(#env, principal) or @permissionEnvironnement.canWrite(#env, principal)")
    @GetMapping(value = "/{env}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<EnvironnementDto> get(@PathVariable Long env) {
        LOGGER.debug("REST request to get one environnement {}", env);
        Optional<EnvironnementDto> environnementDtoOptional = environnementService.findEnvironnement(env)
                .map(environnement -> mapper.map(environnement, EnvironnementDto.class));
        return ResponseUtil.wrapOrNotFound(environnementDtoOptional);
    }

    @PreAuthorize("@permissionEnvironnement.canWrite(#env, principal)")
    @GetMapping(value = "/purgeLog/{env}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> purgeLog(@PathVariable Long env) {
        LOGGER.debug("REST request to purge log");
        environnementService.purgerLogs(env);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("@permissionEnvironnement.canWrite(#env, principal)")
    @GetMapping(value = "/purgeArchive/{env}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> purgeArchive(@PathVariable Long env) {
        LOGGER.debug("REST request to purge log");
        environnementService.purgerArchive(env);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * POST  /environnement/info/{env} to change date traitement of environnement.
     */
    @PreAuthorize("@permissionEnvironnement.canRead(#env, principal)")
    @GetMapping(value = "/dateTraitement/{env}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<String> changeDateTraitement(@PathVariable Long env, @RequestParam @DateTimeFormat(pattern = "dd/MM/yyyy") Date dateTraitement) {
        LOGGER.debug("REST request to set new date traitement of environnement");
        if (environnementService.changeDateTraiement(env, dateTraitement)) {
            return ResponseEntity.status(HttpStatus.OK).body(null);
        }
        return ResponseEntity.badRequest().build();
    }

    /**
     * PUT  /environnement to add a new environnement
     */
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("@permissionApplication.canWrite(#env.application, principal) && @permissionServiceOpen.canCreateEnvironment()")
    public ResponseEntity<EnvironnementDto> addEnvironnement(@RequestBody EnvironnementCreationDto env) {
        LOGGER.debug("REST request to add a new environnement {}", env);
        if (env.getPrefix() == null) {
            env.setPrefix("");
        }
        Environnement environnement = environnementService.saveEnvironnement(mapper.map(env, Environnement.class));
        return new ResponseEntity<>(mapper.map(environnement, EnvironnementDto.class), HttpStatus.OK);
    }

    /**
     * PATCH  /environnement to update an environnement
     */
    @PatchMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("@permissionEnvironnement.canWrite(#env, principal)")
    public ResponseEntity<EnvironnementDto> updateEnvironnement(@RequestBody EnvironnementDto env) {
        LOGGER.debug("REST request to update an environnement {}", env.getId());
        Environnement result = environnementService.updateEnvironnement(mapper.map(env, Environnement.class));
        return new ResponseEntity<>(mapper.map(result, EnvironnementDto.class), HttpStatus.OK);
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
    public List<EnvironnementDto> importEnvApp(@PathVariable Long id) throws EbadServiceException {
        LOGGER.debug("REST request to import all env for app {} ", id);
        return mapper.mapAsList(environnementService.importEnvironments(id), EnvironnementDto.class);
    }

    @PostMapping(value = "/import-all", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<EnvironnementDto> importAll() throws EbadServiceException {
        LOGGER.debug("REST request to import all Environments ");
        return mapper.mapAsList(environnementService.importEnvironments(), EnvironnementDto.class);
    }
}
