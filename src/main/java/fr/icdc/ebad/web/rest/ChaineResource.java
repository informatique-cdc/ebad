package fr.icdc.ebad.web.rest;

import com.querydsl.core.types.Predicate;
import fr.icdc.ebad.domain.Chaine;
import fr.icdc.ebad.domain.Environnement;
import fr.icdc.ebad.security.SecurityUtils;
import fr.icdc.ebad.service.ChaineService;
import fr.icdc.ebad.service.util.EbadServiceException;
import fr.icdc.ebad.web.rest.dto.ChaineDto;
import fr.icdc.ebad.web.rest.dto.ChaineSimpleDto;
import fr.icdc.ebad.web.rest.dto.JobDto;
import fr.icdc.ebad.web.rest.util.PaginationUtil;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.tags.Tag;
import ma.glasnost.orika.MapperFacade;
import org.jobrunr.jobs.JobId;
import org.jobrunr.scheduling.JobScheduler;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chains")
@Tag(name = "Chaines", description = "the chaines API")
public class ChaineResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChaineResource.class);

    private final ChaineService chaineService;
    private final MapperFacade mapper;
    private final JobScheduler jobScheduler;

    public ChaineResource(ChaineService chaineService, MapperFacade mapper, JobScheduler jobScheduler) {
        this.chaineService = chaineService;
        this.mapper = mapper;
        this.jobScheduler = jobScheduler;
    }

    /**
     * GET  /chaines/env/:env to get all chaine from env.
     */
    @GetMapping(value = "/env/{env}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("@permissionEnvironnement.canRead(#env,principal) or @permissionEnvironnement.canWrite(#env,principal)")
    public ResponseEntity<Page<ChaineDto>> getAllFromEnv(Pageable pageable, @QuerydslPredicate(root = Chaine.class) Predicate predicate, @PathVariable Long env) {
        LOGGER.debug("REST request to get all Chaines from environnement {}", env);
        Environnement environnement = Environnement.builder().id(env).build();
        Page<Chaine> page = chaineService.getAllChaineFromEnvironmentWithPageable(predicate, PaginationUtil.generatePageRequestOrDefault(pageable), environnement);

        return new ResponseEntity<>(page.map((chain) -> mapper.map(chain, ChaineDto.class)), HttpStatus.OK);
    }

    /**
     * POST chaines/:id/run to run chaine
     */
    @PreAuthorize("@permissionChaine.canRead(#id,principal)")
    @PostMapping(value = "/{id}/run", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<JobDto> runChaine(@PathVariable Long id) throws EbadServiceException {
        LOGGER.debug("REST request to run chaine");
        JobId jobId = jobScheduler.enqueue(() -> chaineService.jobRunChaine(id, SecurityUtils.getCurrentLogin()));
        LOGGER.debug("job id is " + jobId);

        return new ResponseEntity<>(JobDto.builder().id(jobId.asUUID()).build(), HttpStatus.OK);
    }

    /**
     * PUT  /chaines to add a new chaine
     */
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("@permissionEnvironnement.canWrite(#chaineDto.environnement.id,principal)")
    public ChaineSimpleDto addChaine(@RequestBody ChaineDto chaineDto) {
        LOGGER.debug("REST request to add a new chaine");
        Chaine chaine = chaineService.addChaine(mapper.map(chaineDto, Chaine.class));
        return mapper.map(chaine, ChaineSimpleDto.class);
    }

    /**
     * DELETE  /chaines/delete/{id} to delete a chaine with given id
     */
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("@permissionChaine.canWrite(#id,principal)")
    public ResponseEntity<Void> removeChaine(@PathVariable Long id) {
        LOGGER.debug("REST request to remove a chaine");
        chaineService.deleteChaine(id);
        return ResponseEntity.ok().build();
    }

    /**
     * PATCH  /chaines to update a chaine
     */
    @PatchMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("@permissionChaine.canWrite(#chaineDto,principal)")
    public ResponseEntity<ChaineSimpleDto> updateChaine(@RequestBody ChaineDto chaineDto) {
        LOGGER.debug("REST request to update a chaine");
        Chaine chaine = chaineService.updateChaine(mapper.map(chaineDto, Chaine.class));
        return new ResponseEntity<>(mapper.map(chaine, ChaineSimpleDto.class), HttpStatus.OK);
    }
}
