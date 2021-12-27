package fr.icdc.ebad.web.rest;


import fr.icdc.ebad.domain.AccreditationRequest;
import fr.icdc.ebad.service.AccreditationRequestService;
import fr.icdc.ebad.service.util.EbadServiceException;
import fr.icdc.ebad.web.rest.dto.AccreditationRequestDto;
import fr.icdc.ebad.web.rest.dto.CreationAccreditationRequestDto;
import fr.icdc.ebad.web.rest.dto.ResponseAccreditationRequestDto;
import fr.icdc.ebad.web.rest.mapstruct.MapStructMapper;
import fr.icdc.ebad.web.rest.util.PaginationUtil;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/accreditation-requests")
@Tag(name = "Accreditation Request", description = "the accreditation request API")
public class AccreditationRequestResource {
    private final AccreditationRequestService accreditationRequestService;
    private final MapStructMapper mapStructMapper;

    public AccreditationRequestResource(AccreditationRequestService accreditationRequestService, MapStructMapper mapStructMapper) {
        this.accreditationRequestService = accreditationRequestService;
        this.mapStructMapper = mapStructMapper;
    }

    @GetMapping("/need-answer")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    @PageableAsQueryParam
    public Page<AccreditationRequestDto> findAll(@Parameter(hidden = true) Pageable pageable) {
        Page<AccreditationRequest> accreditationRequests = accreditationRequestService.getAllAccreditationRequestToAnswer(PaginationUtil.generatePageRequestOrDefault(pageable));
        return accreditationRequests.map(mapStructMapper::convert);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    @PageableAsQueryParam
    public Page<AccreditationRequestDto> findAllMyRequest(@Parameter(hidden = true) Pageable pageable) {
        Page<AccreditationRequest> accreditationRequests = accreditationRequestService.getMyAccreditationRequest(PaginationUtil.generatePageRequestOrDefault(pageable));
        return accreditationRequests.map(mapStructMapper::convert);
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<AccreditationRequestDto> createAccreditationRequest(@RequestBody @Valid CreationAccreditationRequestDto creationAccreditationRequestDto) throws EbadServiceException {
        AccreditationRequest accreditationRequest = accreditationRequestService.requestNewAccreditation(creationAccreditationRequestDto.getApplicationId(), creationAccreditationRequestDto.isWantManage(), creationAccreditationRequestDto.isWantUse());
        return ResponseEntity.ok(mapStructMapper.convert(accreditationRequest));
    }

    @PostMapping("/response")
    @PreAuthorize("@permissionAccreditationRequest.canAcceptAccreditationRequest(#responseAccreditationRequestDto.id, principal)")
    public ResponseEntity<Void> answerRequest(@RequestBody @Valid ResponseAccreditationRequestDto responseAccreditationRequestDto, BindingResult result) throws EbadServiceException {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().build();
        }
        accreditationRequestService.answerToRequest(responseAccreditationRequestDto.getId(), responseAccreditationRequestDto.isAccepted());
        return ResponseEntity.ok().build();
    }
}
