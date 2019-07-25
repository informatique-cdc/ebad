package fr.icdc.ebad.web.rest;

import fr.icdc.ebad.service.LogBatchService;
import fr.icdc.ebad.web.rest.dto.LogBatchDto;
import fr.icdc.ebad.web.rest.util.PaginationUtil;
import io.micrometer.core.annotation.Timed;
import ma.glasnost.orika.MapperFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for view and managing Log Level at runtime.
 */
@RestController
@RequestMapping("/api")
public class LogsResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogsResource.class);

    private final LogBatchService logBatchService;
    private final MapperFacade mapper;

    public LogsResource(LogBatchService logBatchService, MapperFacade mapper) {
        this.mapper = mapper;
        this.logBatchService = logBatchService;
    }

    @GetMapping(value = "/logs/", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Page<LogBatchDto> getAllLog(@RequestParam(value = "page", required = false) Integer offset,
                                       @RequestParam(value = "per_page", required = false) Integer limit) {
        LOGGER.debug("get all log");
        return logBatchService.getAllLogBatchWithPageable(PaginationUtil.generatePageRequest(offset, limit))
                .map(logBatch -> mapper.map(logBatch, LogBatchDto.class));
    }

    @GetMapping(value = "/logs/{env}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("@permissionEnvironnement.canRead(#env, principal)")
    public Page<LogBatchDto> getAllLogFromEnv(@PathVariable Long env,
                                              @RequestParam(value = "page", required = false) Integer offset,
                                              @RequestParam(value = "per_page", required = false) Integer limit) {
        LOGGER.debug("get all log from env {}",env);
        return logBatchService.getAllLogBatchFromEnvironmentWithPageable(PaginationUtil.generatePageRequest(offset, limit), env)
                .map(logBatch -> mapper.map(logBatch, LogBatchDto.class));
    }

    @GetMapping(value = "/logs/{env}/{batch}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("@permissionEnvironnement.canRead(#env, principal)")
    public Page<LogBatchDto> getAllLogFromEnvBatch(@PathVariable Long env,
                                                   @PathVariable Long batch,
                                                   @RequestParam(value = "page", required = false) Integer offset,
                                                   @RequestParam(value = "per_page", required = false) Integer limit) {
        LOGGER.debug("get all log from env {}",env);

        return logBatchService
                .getAllLogBatchFromEnvironmentAndBatchWithPageable(
                        PaginationUtil.generatePageRequest(offset, limit), env, batch
                )
                .map(logBatch -> mapper.map(logBatch, LogBatchDto.class));
    }
}
