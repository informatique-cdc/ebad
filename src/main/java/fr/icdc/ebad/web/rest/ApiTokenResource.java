package fr.icdc.ebad.web.rest;

import fr.icdc.ebad.domain.ApiToken;
import fr.icdc.ebad.security.SecurityUtils;
import fr.icdc.ebad.service.ApiTokenService;
import fr.icdc.ebad.service.util.EbadServiceException;
import fr.icdc.ebad.web.rest.dto.ApiTokenDto;
import fr.icdc.ebad.web.rest.dto.ApiTokenWithKeyDto;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.tags.Tag;
import ma.glasnost.orika.MapperFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api-token")
@Tag(name = "API Token", description = "the API token API")
public class ApiTokenResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiTokenResource.class);

    private final ApiTokenService apiTokenService;
    private final MapperFacade mapper;

    public ApiTokenResource(ApiTokenService apiTokenService, MapperFacade mapper) {
        this.apiTokenService = apiTokenService;
        this.mapper = mapper;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<Page<ApiTokenDto>> findToken(Pageable pageable) {
        LOGGER.debug("REST request to find api token");
        Page<ApiToken> apiTokens = apiTokenService.findTokenByUser(SecurityUtils.getCurrentLogin(), pageable);
        return ResponseEntity.ok().body(apiTokens.map(apiToken -> mapper.map(apiToken, ApiTokenDto.class)));
    }

    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<ApiTokenWithKeyDto> createToken(@Valid ApiTokenDto apiTokenDto) throws EbadServiceException {
        LOGGER.debug("REST request to create api token");
        ApiToken apiToken = apiTokenService.createToken(SecurityUtils.getCurrentLogin(), apiTokenDto.getName());
        return ResponseEntity.ok().body(mapper.map(apiToken, ApiTokenWithKeyDto.class));
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("@permissionApiKey.canReadWrite(#id, principal)")
    public ResponseEntity<Void> deleteToken(@PathVariable Long id) {
        LOGGER.debug("REST request to delete api token {}", id);
        apiTokenService.deleteToken(id);
        return ResponseEntity.ok().build();
    }
}
