package fr.icdc.ebad.web.rest;


import fr.icdc.ebad.domain.AccreditationRequest;
import fr.icdc.ebad.service.AccreditationRequestService;
import fr.icdc.ebad.service.util.EbadServiceException;
import fr.icdc.ebad.web.rest.dto.AccreditationRequestDto;
import fr.icdc.ebad.web.rest.dto.CreationAccreditationRequestDto;
import fr.icdc.ebad.web.rest.dto.ResponseAccreditationRequestDto;
import fr.icdc.ebad.web.rest.util.PaginationUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import ma.glasnost.orika.MapperFacade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/accreditation-requests")
@Tag(name = "Accreditation Request", description = "the accreditation request API")
public class AccreditationRequestResource {
    private final AccreditationRequestService accreditationRequestService;
    private final MapperFacade mapperFacade;

    public AccreditationRequestResource(AccreditationRequestService accreditationRequestService, MapperFacade mapperFacade) {
        this.accreditationRequestService = accreditationRequestService;
        this.mapperFacade = mapperFacade;
    }

    @GetMapping("/need-answer")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public Page<AccreditationRequestDto> findAll(Pageable pageable) {
        Page<AccreditationRequest> accreditationRequests = accreditationRequestService.getAllAccreditationRequestToAnswer(PaginationUtil.generatePageRequestOrDefault(pageable));
        return accreditationRequests.map(accreditationRequest -> mapperFacade.map(accreditationRequest, AccreditationRequestDto.class));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public Page<AccreditationRequestDto> findAllMyRequest(Pageable pageable) {
        Page<AccreditationRequest> accreditationRequests = accreditationRequestService.getMyAccreditationRequest(PaginationUtil.generatePageRequestOrDefault(pageable));
        return accreditationRequests.map(accreditationRequest -> mapperFacade.map(accreditationRequest, AccreditationRequestDto.class));
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<AccreditationRequestDto> createAccreditationRequest(@RequestBody @Valid CreationAccreditationRequestDto creationAccreditationRequestDto, BindingResult result) throws EbadServiceException {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().build();
        }
        AccreditationRequest accreditationRequest = accreditationRequestService.requestNewAccreditation(creationAccreditationRequestDto.getApplicationId(), creationAccreditationRequestDto.isWantManage(), creationAccreditationRequestDto.isWantUse());
        return ResponseEntity.ok(mapperFacade.map(accreditationRequest, AccreditationRequestDto.class));
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
