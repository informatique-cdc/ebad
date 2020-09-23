package fr.icdc.ebad.web.rest;

import com.jcraft.jsch.JSchException;
import com.querydsl.core.types.Predicate;
import fr.icdc.ebad.domain.Batch;
import fr.icdc.ebad.domain.util.RetourBatch;
import fr.icdc.ebad.service.BatchService;
import fr.icdc.ebad.service.util.EbadServiceException;
import fr.icdc.ebad.web.rest.dto.BatchDto;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/batchs")
@Tag(name = "Batch", description = "the batch API")
public class BatchResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchResource.class);

    private final BatchService batchService;
    private final MapperFacade mapper;

    public BatchResource(BatchService batchService, MapperFacade mapper) {
        this.batchService = batchService;
        this.mapper = mapper;
    }

    /**
     * GET  /batchs to get all batch with predicate.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public Page<BatchDto> getByPredicate(@QuerydslPredicate(root = Batch.class) Predicate predicate, Pageable pageable) {
        LOGGER.debug("REST request to get Batchs ");
        return batchService.getAllBatchWithPredicate(predicate, PaginationUtil.generatePageRequestOrDefault(pageable)).map(batch -> mapper.map(batch, BatchDto.class));
    }

    /**
     * GET  /batchs/run/:id to run batch
     */
    @PreAuthorize("@permissionEnvironnement.canRead(#env, principal)")
    @GetMapping(value = "/run/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<RetourBatch> runBatch(@PathVariable Long id, @RequestParam(value = "param", required = false) String param, @RequestParam Long env) throws EbadServiceException {
        LOGGER.debug("REST request to run batch");
        try {
            return new ResponseEntity<>(batchService.runBatch(id, env, param), HttpStatus.OK);
        } catch (JSchException | IOException e) {
            LOGGER.error("Erreur lors de l'exécution du batch", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * PUT  /batchs to add a new batch
     */
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("@permissionBatch.canWrite(#batchDto, principal)")
    public ResponseEntity<BatchDto> addBatch(@RequestBody BatchDto batchDto) {
        LOGGER.debug("REST request to add a new batch");
        Batch batch = batchService.saveBatch(mapper.map(batchDto, Batch.class));
        return new ResponseEntity<>(mapper.map(batch, BatchDto.class), HttpStatus.OK);
    }

    /**
     * PATCH  /batchs to update a batch
     */
    @PatchMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("@permissionBatch.canWrite(#batchDto, principal)")
    public ResponseEntity<BatchDto> updateBatch(@RequestBody BatchDto batchDto) {
        LOGGER.debug("REST request to update a batch");
        Batch batch = batchService.saveBatch(mapper.map(batchDto, Batch.class));
        return new ResponseEntity<>(mapper.map(batch, BatchDto.class), HttpStatus.OK);
    }

    /**
     * DELETE  /batchs/id to delete a batch
     */
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("@permissionBatch.canWrite(#id, principal)")
    public ResponseEntity<Void> deleteBatch(@PathVariable Long id) {
        LOGGER.debug("REST request to delete a batch");
        batchService.deleteBatch(id);
        return ResponseEntity.ok().build();
    }
}
