package fr.icdc.ebad.web.rest;

import com.querydsl.core.types.Predicate;
import fr.icdc.ebad.domain.Norme;
import fr.icdc.ebad.mapper.MapStructMapper;
import fr.icdc.ebad.service.NormeService;
import fr.icdc.ebad.web.ResponseUtil;
import fr.icdc.ebad.web.rest.dto.NormLabelIdDto;
import fr.icdc.ebad.web.rest.dto.NormeDto;
import fr.icdc.ebad.web.rest.util.PaginationUtil;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/norms")
@Tag(name = "Norms", description = "the norm API")
public class NormResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(NormResource.class);

    private final NormeService normeService;
    private final MapStructMapper mapStructMapper;

    @Autowired
    public NormResource(NormeService normeService, MapStructMapper mapStructMapper) {
        this.normeService = normeService;
        this.mapStructMapper = mapStructMapper;
    }

    /**
     * GET  /norms to get all normes.
     */
    @GetMapping
    @Timed
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @PageableAsQueryParam
    public Page<NormeDto> getAll(@Parameter(hidden = true) Pageable pageable, @QuerydslPredicate(root = Norme.class) Predicate predicate) {
        LOGGER.debug("REST request to get all Norme - Read");
        Page<Norme> normePage = normeService.getAllNormes(predicate, PaginationUtil.generatePageRequestOrDefault(pageable));
        return normePage.map(mapStructMapper::convertToNormeDto);
    }

    /**
     * GET  /norms/name to get all normes.
     */
    @GetMapping("/name")
    @Timed
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    @PageableAsQueryParam
    public Page<NormLabelIdDto> getAllForList(@Parameter(hidden = true) Pageable pageable, @QuerydslPredicate(root = Norme.class) Predicate predicate) {
        LOGGER.debug("REST request to get all Norme - Read");
        Page<Norme> normePage = normeService.getAllNormes(predicate, PaginationUtil.generatePageRequestOrDefault(pageable));
        return normePage.map(mapStructMapper::convertToNormLabelIdDto);
    }

    /**
     * GET  /api/normes/:id to get the given norme
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<NormeDto> getOne(@PathVariable("id") Long id) {
        LOGGER.debug("REST request to get Norme with id {}", id);
        Optional<NormeDto> normeDto = normeService.findNormeById(id).map(mapStructMapper::convertToNormeDto);
        return ResponseUtil.wrapOrNotFound(normeDto);
    }

    /**
     * PUT  /api/normes to save a new norme
     */
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<NormeDto> save(@RequestBody @Valid  NormeDto normeDto) {
        LOGGER.debug("REST request to save Norme");
        if (normeDto.getId() != null) {
            return ResponseEntity.badRequest().build();
        }
        Norme norme = normeService.saveNorme(mapStructMapper.convert(normeDto));
        return ResponseEntity.ok(mapStructMapper.convertToNormeDto(norme));
    }

    /**
     * DELETE  /api/normes/{id} to delete the given norme
     */
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        LOGGER.debug("REST request to delete Norm with id {}", id);
        normeService.deleteNormeById(id);
        return ResponseEntity.ok().build();
    }

    /**
     * PATCH  /api/normes to update a norme
     */
    @PatchMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<NormeDto> update(@RequestBody NormeDto normeDto) {
        LOGGER.debug("REST request to update Norm");
        if (normeDto.getId() == null) {
            return ResponseEntity.badRequest().build();
        }
        Norme norme = normeService.saveNorme(mapStructMapper.convert(normeDto));
        return ResponseEntity.ok(mapStructMapper.convertToNormeDto(norme));
    }
}
