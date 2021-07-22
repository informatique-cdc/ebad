package fr.icdc.ebad.web.rest;

import com.querydsl.core.types.Predicate;
import fr.icdc.ebad.domain.Batch;
import fr.icdc.ebad.security.SecurityUtils;
import fr.icdc.ebad.service.BatchService;
import fr.icdc.ebad.web.rest.dto.BatchDto;
import fr.icdc.ebad.web.rest.dto.CurrentJobDto;
import fr.icdc.ebad.web.rest.dto.JobDto;
import fr.icdc.ebad.web.rest.util.PaginationUtil;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import ma.glasnost.orika.MapperFacade;
import org.jobrunr.jobs.JobId;
import org.jobrunr.scheduling.JobScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.api.annotations.ParameterObject;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.UUID;

@RestController
@RequestMapping("/batchs")
@Tag(name = "Batch", description = "the batch API")
public class BatchResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchResource.class);

    private final BatchService batchService;
    private final MapperFacade mapper;
    private final JobScheduler jobScheduler;

    public BatchResource(BatchService batchService, MapperFacade mapper, JobScheduler jobScheduler) {
        this.batchService = batchService;
        this.mapper = mapper;
        this.jobScheduler = jobScheduler;
    }

    /**
     * GET  /batchs to get all batch with predicate.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    @PageableAsQueryParam
    public Page<BatchDto> getByPredicate(@QuerydslPredicate(root = Batch.class) Predicate predicate, @Parameter(hidden = true) Pageable pageable) {
        LOGGER.debug("REST request to get Batchs ");
        return batchService.getAllBatchWithPredicate(predicate, PaginationUtil.generatePageRequestOrDefault(pageable)).map(batch -> mapper.map(batch, BatchDto.class));
    }

    /**
     * GET  /batchs/run/:id to run batch
     */
    @PreAuthorize("@permissionEnvironnement.canRead(#env, principal)")
    @GetMapping(value = "/run/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<JobDto> runBatch(@PathVariable long id, @RequestParam(value = "param", required = false) String param, @RequestParam long env) {
        LOGGER.debug("REST request to run batch");
        JobId jobId;

        UUID uuid = UUID.randomUUID();
        if (param != null) {
            jobId = jobScheduler.enqueue(uuid, () -> batchService.jobRunBatch(id, env, param, SecurityUtils.getCurrentLogin(), uuid));
        } else {
            jobId = jobScheduler.enqueue(uuid, () -> batchService.jobRunBatch(id, env, SecurityUtils.getCurrentLogin(), uuid));
        }
        LOGGER.debug("job id is {}", jobId);
        return new ResponseEntity<>(JobDto.builder().id(jobId.asUUID()).build(), HttpStatus.OK);
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

    @PreAuthorize("@permissionEnvironnement.canRead(#id, principal)")
    @GetMapping(path = "/state/{id}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<CurrentJobDto> streamFlux(@PathVariable Long id) {
        return Flux.interval(Duration.ofSeconds(1))
                .map(sequence -> CurrentJobDto.builder().batchs(batchService.getCurrentJobForEnv(id)).build());
    }
}
