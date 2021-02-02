package fr.icdc.ebad.web.rest;


import fr.icdc.ebad.domain.Scheduling;
import fr.icdc.ebad.service.SchedulingService;
import fr.icdc.ebad.service.util.EbadServiceException;
import fr.icdc.ebad.web.rest.dto.CreationSchedulingDto;
import fr.icdc.ebad.web.rest.dto.SchedulingDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import ma.glasnost.orika.MapperFacade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.net.URI;

@RestController
@RequestMapping("/schedulings")
@Tag(name = "Scheduling", description = "the scheduling API")
public class SchedulingResource {
    private final SchedulingService schedulingService;
    private final MapperFacade mapper;

    public SchedulingResource(SchedulingService schedulingService, MapperFacade mapper) {
        this.schedulingService = schedulingService;
        this.mapper = mapper;
    }

    @PutMapping
    @PreAuthorize("@permissionEnvironnement.canRead(#scheduledDto.environmentId, principal)")
    public ResponseEntity<SchedulingDto> addScheduling(@RequestBody @Valid CreationSchedulingDto scheduledDto) throws EbadServiceException {
        Scheduling scheduling = schedulingService.saveAndRun(scheduledDto.getBatchId(), scheduledDto.getEnvironmentId(), scheduledDto.getParameters(), scheduledDto.getCron());
        SchedulingDto schedulingDto = mapper.map(scheduling, SchedulingDto.class);
        return ResponseEntity.created(URI.create("/core/scheduling/" + schedulingDto.getId())).body(schedulingDto);
    }

    @GetMapping("/env/{environmentId}")
    @PreAuthorize("@permissionEnvironnement.canRead(#environmentId, principal)")
    public Page<SchedulingDto> listByEnvironment(@PathVariable Long environmentId, @PageableDefault Pageable pageable) {
        Page<Scheduling> schedulings = schedulingService.listByEnvironment(environmentId, pageable);
        return schedulings.map(scheduling -> mapper.map(scheduling, SchedulingDto.class));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<SchedulingDto> listAll(@PageableDefault Pageable pageable) {
        Page<Scheduling> schedulings = schedulingService.listAll(pageable);
        return schedulings.map(scheduling -> mapper.map(scheduling, SchedulingDto.class));
    }

    @GetMapping("/{schedulingId}")
    @PreAuthorize("@permissionScheduling.canRead(#schedulingId, principal)")
    public ResponseEntity<SchedulingDto> get(@PathVariable Long schedulingId) {
        Scheduling scheduling = schedulingService.get(schedulingId);
        SchedulingDto schedulingDto = mapper.map(scheduling, SchedulingDto.class);
        return ResponseEntity.ok(schedulingDto);
    }

    @DeleteMapping("/{schedulingId}")
    @PreAuthorize("@permissionScheduling.canRead(#schedulingId, principal)")
    public void delete(@PathVariable Long schedulingId) {
        schedulingService.remove(schedulingId);
    }
}
