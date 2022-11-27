package fr.icdc.ebad.web.rest;

import fr.icdc.ebad.domain.ApiToken;
import fr.icdc.ebad.mapper.MapStructMapper;
import fr.icdc.ebad.security.SecurityUtils;
import fr.icdc.ebad.service.ApiTokenService;
import fr.icdc.ebad.service.util.EbadServiceException;
import fr.icdc.ebad.web.rest.dto.ApiTokenDto;
import fr.icdc.ebad.web.rest.dto.ApiTokenWithKeyDto;
import fr.icdc.ebad.web.rest.util.PaginationUtil;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api-tokens")
@Tag(name = "API Token", description = "the API token API")
public class ApiTokenResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiTokenResource.class);

    private final ApiTokenService apiTokenService;
    private final MapStructMapper mapStructMapper;

    public ApiTokenResource(ApiTokenService apiTokenService, MapStructMapper mapStructMapper) {
        this.apiTokenService = apiTokenService;
        this.mapStructMapper = mapStructMapper;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    @PageableAsQueryParam
    public ResponseEntity<Page<ApiTokenDto>> findToken(@Parameter(hidden = true) Pageable pageable) {
        LOGGER.debug("REST request to find api token");
        Page<ApiToken> apiTokens = apiTokenService.findTokenByUser(SecurityUtils.getCurrentLogin(), PaginationUtil.generatePageRequestOrDefault(pageable));
        return ResponseEntity.ok().body(apiTokens.map(mapStructMapper::convert));
    }

    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public ResponseEntity<ApiTokenWithKeyDto> createToken(@Valid @RequestBody ApiTokenDto apiTokenDto) throws EbadServiceException {
        LOGGER.debug("REST request to create api token");
        ApiToken apiToken = apiTokenService.createToken(SecurityUtils.getCurrentLogin(), apiTokenDto.getName());
        return ResponseEntity.ok().body(mapStructMapper.convertToTokenWithKeyDto(apiToken));
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
