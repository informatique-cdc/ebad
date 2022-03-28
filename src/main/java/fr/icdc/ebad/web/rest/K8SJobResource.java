package fr.icdc.ebad.web.rest;

import fr.icdc.ebad.mapper.MapStructMapper;
import fr.icdc.ebad.service.K8SJobService;
import fr.icdc.ebad.web.rest.dto.BatchDto;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/k8s/jobs")
@Tag(name = "K8S Job", description = "the K8S Job API")
public class K8SJobResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(K8SJobResource.class);

    private final K8SJobService k8SJobService;
    private final MapStructMapper mapStructMapper;

    public K8SJobResource(K8SJobService k8SJobService, MapStructMapper mapStructMapper) {
        this.k8SJobService = k8SJobService;
        this.mapStructMapper = mapStructMapper;
    }


    /**
     * GET  /k8s/jobs/run/:id to run batch
     */
    @PreAuthorize("@permissionEnvironnement.canRead(#env, principal)")
    @GetMapping(value = "/run/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Void> runBatch(@PathVariable long id, @RequestParam(value = "param", required = false) String param, @RequestParam long env) {
        LOGGER.debug("REST request to run k8s job");
        return new ResponseEntity<>(HttpStatus.OK);
    }


    /**
     * PUT  /k8s/jobs to add a new batch
     */
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("@permissionBatch.canWrite(#batchDto, principal)")
    public ResponseEntity<Void> addBatch(@RequestBody @Valid BatchDto batchDto) {
        LOGGER.debug("REST request to add a new batch");
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * PATCH  /k8s/jobs to update a batch
     */
    @PatchMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("@permissionBatch.canWrite(#batchDto, principal)")
    public ResponseEntity<Void> updateBatch(@RequestBody BatchDto batchDto) {
        LOGGER.debug("REST request to update a batch");
        return new ResponseEntity<>( HttpStatus.OK);
    }

    /**
     * DELETE  /k8s/jobs/id to delete a batch
     */
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("@permissionBatch.canWrite(#id, principal)")
    public ResponseEntity<Void> deleteBatch(@PathVariable Long id) {
        LOGGER.debug("REST request to delete a batch");
        return ResponseEntity.ok().build();
    }


}
