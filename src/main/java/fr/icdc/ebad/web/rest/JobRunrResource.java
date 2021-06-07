package fr.icdc.ebad.web.rest;

import fr.icdc.ebad.service.JobRunrService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/jobrunr"})
@Tag(
        name = "JobRunr",
        description = "the JobRunr Admin API"
)
public class JobRunrResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(JobRunrResource.class);

    private final JobRunrService jobRunrService;

    public JobRunrResource(JobRunrService jobRunrService) {
        this.jobRunrService = jobRunrService;
    }

    @PostMapping("/restart")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ResponseEntity<Void> restartJobRunr() {
        LOGGER.debug("REST request to restart jobrunr");
        jobRunrService.restartJobRunr();
        return ResponseEntity.ok().build();
    }
}
