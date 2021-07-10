package fr.icdc.ebad.web.rest;

import com.querydsl.core.types.Predicate;
import fr.icdc.ebad.domain.LogBatch;
import fr.icdc.ebad.domain.QLogBatch;
import fr.icdc.ebad.service.JobRunrService;
import fr.icdc.ebad.service.LogBatchService;
import fr.icdc.ebad.web.rest.dto.JobStateDto;
import fr.icdc.ebad.web.rest.dto.LogBatchDto;
import fr.icdc.ebad.web.rest.util.PaginationUtil;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.tags.Tag;
import ma.glasnost.orika.MapperFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

/**
 * Controller for view and managing Log Level at runtime.
 */
@RestController
@RequestMapping("/logs")
@Tag(name = "Log", description = "the log API")
public class LogsResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogsResource.class);

    private final LogBatchService logBatchService;
    private final MapperFacade mapper;
    private final JobRunrService jobRunrService;

    public LogsResource(LogBatchService logBatchService, MapperFacade mapper, JobRunrService jobRunrService) {
        this.mapper = mapper;
        this.logBatchService = logBatchService;
        this.jobRunrService = jobRunrService;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Page<LogBatchDto> getAllLog(@QuerydslPredicate(root = LogBatch.class) Predicate predicate, Pageable pageable) {
        LOGGER.debug("get all log");
        return logBatchService.getAllLogBatchWithPageable(predicate, pageable)
                .map(logBatch -> mapper.map(logBatch, LogBatchDto.class));
    }

    @GetMapping(value = "/{env}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("@permissionEnvironnement.canRead(#env, principal)")
    public Page<LogBatchDto> getAllLogFromEnv(@PathVariable Long env, @QuerydslPredicate(root = LogBatch.class) Predicate predicate, Pageable pageable) {
        LOGGER.debug("get all log from env {}", env);
        Predicate envPredicate = QLogBatch.logBatch.environnement.id.eq(env).and(predicate);
        return logBatchService.getAllLogBatchWithPageable(envPredicate, PaginationUtil.generatePageRequestOrDefault(pageable))
                .map(logBatch -> mapper.map(logBatch, LogBatchDto.class));
    }

    @GetMapping(value = "/job/{jobId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PostAuthorize("returnObject.body == null || returnObject.body.log == null || @permissionEnvironnement.canRead(returnObject.body.log.environnement.id, principal)")
    public ResponseEntity<JobStateDto> getLogFromJobId(@PathVariable String jobId) {
        LOGGER.debug("get log from jobid {}", jobId);
        JobStateDto jobStateDto = new JobStateDto();
        String state = jobRunrService.getState(UUID.fromString(jobId)).toString();
        jobStateDto.setId(jobId);
        jobStateDto.setState(state);
        Optional<LogBatchDto> logBatchDtoOptional = logBatchService.getByJobId(jobId)
                .map(logBatch -> mapper.map(logBatch, LogBatchDto.class));
        logBatchDtoOptional.ifPresent(jobStateDto::setLog);
        return ResponseEntity.ok(jobStateDto);
    }

    @GetMapping(value = "/{env}/{batch}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("@permissionEnvironnement.canRead(#env, principal)")
    public Page<LogBatchDto> getAllLogFromEnvBatch(@PathVariable Long env,
                                                   @PathVariable Long batch,
                                                   @QuerydslPredicate(root = LogBatch.class) Predicate predicate, Pageable pageable
    ) {
        LOGGER.debug("get all log from env {}", env);

        Predicate envBatchPredicate = QLogBatch.logBatch.environnement.id.eq(env)
                .and(QLogBatch.logBatch.batch.id.eq(batch))
                .and(predicate);

        return logBatchService
                .getAllLogBatchWithPageable(envBatchPredicate, PaginationUtil.generatePageRequestOrDefault(pageable)
                )
                .map(logBatch -> mapper.map(logBatch, LogBatchDto.class));
    }
}
