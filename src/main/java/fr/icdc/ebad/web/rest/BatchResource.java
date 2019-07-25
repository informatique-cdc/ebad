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
import ma.glasnost.orika.MapperFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("/api")
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
    @GetMapping(value = "/batchs", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PostFilter("@permissionEnvironnement.canReadEnvironnements(filterObject.environnements, principal) or @permissionEnvironnement.canWriteEnvironnements(filterObject.environnements, principal)")
    public List<BatchDto> getByPredicate(@QuerydslPredicate(root = Batch.class) Predicate predicate) {
        LOGGER.debug("REST request to get Batchs ");
        return mapper.mapAsList(batchService.getAllBatchWithPredicate(predicate), BatchDto.class);
    }

    /**
     * GET  /batchs/env/:env to get all batch from env.
     */
    @GetMapping(value = "/batchs/env/{env}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("@permissionEnvironnement.canRead(#env, principal) or @permissionEnvironnement.canWrite(#env, principal)")
    public ResponseEntity<List<BatchDto>> getAllFromEnv(@RequestParam(value = "page", required = false) Integer offset, @RequestParam(value = "per_page", required = false) Integer limit, @PathVariable Long env) throws URISyntaxException {
        LOGGER.debug("REST request to get all Batchs from environnement {}", env);
        Page<Batch> page = batchService.getAllBatchFromEnvironmentAsPage(env, PaginationUtil.generatePageRequest(offset, limit));
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/batchs/env/" + env, offset, limit);
        return new ResponseEntity<>(mapper.mapAsList(page.getContent(), BatchDto.class), headers, HttpStatus.OK);
    }

    /**
     * GET  /batchs/run/:id to run batch
     */
    @PreAuthorize("@permissionEnvironnement.canRead(#env, principal)")
    @GetMapping(value = "/batchs/run/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
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
    @PutMapping(value = "/batchs", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("@permissionBatch.canWrite(#batchDto, principal)")
    public ResponseEntity<BatchDto> addBatch(@RequestBody BatchDto batchDto) {
        LOGGER.debug("REST request to add a new batch");
        Batch batch = batchService.saveBatch(mapper.map(batchDto, Batch.class));
        return new ResponseEntity<>(mapper.map(batch, BatchDto.class), HttpStatus.OK);
    }

    /**
     * POST  /batchs/delete to delete a batch
     */
    @PostMapping(value = "/batchs/delete/{env}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("@permissionBatch.canWrite(#batchDto, principal)")
    public ResponseEntity<Batch> removeBatch(@RequestBody BatchDto batchDto) {
        LOGGER.debug("REST request to remove a  batch");
        batchService.deleteBatch(batchDto.getId());
        return new ResponseEntity<>(HttpStatus.OK);
    }


    /**
     * PATCH  /batchs to update a batch
     */
    @PatchMapping(value = "/batchs", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("@permissionBatch.canWrite(#batchDto, principal)")
    public ResponseEntity<BatchDto> updateBatch(@RequestBody BatchDto batchDto) {
        LOGGER.debug("REST request to update a batch");
        Batch batch = batchService.saveBatch(mapper.map(batchDto, Batch.class));
        return new ResponseEntity<>(mapper.map(batch, BatchDto.class), HttpStatus.OK);
    }
}
