package fr.icdc.ebad.web.rest;


import fr.icdc.ebad.domain.Scheduling;
import fr.icdc.ebad.service.SchedulingService;
import fr.icdc.ebad.service.util.EbadServiceException;
import fr.icdc.ebad.web.rest.dto.CreationSchedulingDto;
import fr.icdc.ebad.web.rest.dto.SchedulingDto;
import fr.icdc.ebad.mapper.MapStructMapper;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;

@RestController
@RequestMapping("/schedulings")
@Tag(name = "Scheduling", description = "the scheduling API")
public class SchedulingResource {
    private final SchedulingService schedulingService;
    private final MapStructMapper mapStructMapper;

    public SchedulingResource(SchedulingService schedulingService, MapStructMapper mapStructMapper) {
        this.schedulingService = schedulingService;
        this.mapStructMapper = mapStructMapper;
    }

    @PutMapping
    @PreAuthorize("@permissionEnvironnement.canRead(#scheduledDto.environmentId, principal)")
    public ResponseEntity<SchedulingDto> addScheduling(@RequestBody @Valid CreationSchedulingDto scheduledDto) throws EbadServiceException {
        Scheduling scheduling = schedulingService.saveAndRun(scheduledDto.getBatchId(), scheduledDto.getEnvironmentId(), scheduledDto.getParameters(), scheduledDto.getCron());
        SchedulingDto schedulingDto = mapStructMapper.convert(scheduling);
        return ResponseEntity.created(URI.create("/core/scheduling/" + schedulingDto.getId())).body(schedulingDto);
    }

    @GetMapping("/env/{environmentId}")
    @PreAuthorize("@permissionEnvironnement.canRead(#environmentId, principal)")
    @PageableAsQueryParam
    public Page<SchedulingDto> listByEnvironment(@PathVariable Long environmentId, @PageableDefault @Parameter(hidden = true) Pageable pageable) {
        Page<Scheduling> schedulings = schedulingService.listByEnvironment(environmentId, pageable);
        return schedulings.map(mapStructMapper::convert);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @PageableAsQueryParam
    public Page<SchedulingDto> listAll(@PageableDefault @Parameter(hidden = true) Pageable pageable) {
        Page<Scheduling> schedulings = schedulingService.listAll(pageable);
        return schedulings.map(mapStructMapper::convert);
    }

    @GetMapping("/{schedulingId}")
    @PreAuthorize("@permissionScheduling.canRead(#schedulingId, principal)")
    public ResponseEntity<SchedulingDto> get(@PathVariable Long schedulingId) {
        Scheduling scheduling = schedulingService.get(schedulingId);
        SchedulingDto schedulingDto = mapStructMapper.convert(scheduling);
        return ResponseEntity.ok(schedulingDto);
    }

    @DeleteMapping("/{schedulingId}")
    @PreAuthorize("@permissionScheduling.canRead(#schedulingId, principal)")
    public void delete(@PathVariable Long schedulingId) {
        schedulingService.remove(schedulingId);
    }
}
