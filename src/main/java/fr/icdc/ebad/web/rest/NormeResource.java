package fr.icdc.ebad.web.rest;

import fr.icdc.ebad.domain.Norme;
import fr.icdc.ebad.service.NormeService;
import fr.icdc.ebad.web.ResponseUtil;
import fr.icdc.ebad.web.rest.dto.NormeDto;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.tags.Tag;
import ma.glasnost.orika.MapperFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
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

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/norms")
@Tag(name = "Norms", description = "the norm API")
public class NormeResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(NormeResource.class);

    private final NormeService normeService;
    private final MapperFacade mapper;

    @Autowired
    public NormeResource(NormeService normeService, MapperFacade mapper) {
        this.normeService = normeService;
        this.mapper = mapper;
    }

    /**
     * GET  /api/normes to get all normes.
     */
    @GetMapping
    @Timed
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_MODO')")
    public List<NormeDto> getAll() {
        LOGGER.debug("REST request to get all Norme - Read");
        List<Norme> normeList = normeService.getAllNormesSorted(new Sort(Sort.Direction.ASC, "name"));
        return mapper.mapAsList(normeList, NormeDto.class);
    }

    /**
     * GET  /api/normes/:id to get the given norme
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<NormeDto> getOne(@PathVariable("id") Long id) {
        LOGGER.debug("REST request to get Norme with id {}", id);
        Optional<NormeDto> normeDto = normeService.findNormeById(id).map(norme -> mapper.map(norme, NormeDto.class));
        return ResponseUtil.wrapOrNotFound(normeDto);
    }

    /**
     * PUT  /api/normes to save a new norme
     */
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<NormeDto> save(@RequestBody NormeDto normeDto) {
        LOGGER.debug("REST request to save Norme");
        if (normeDto.getId() != null) {
            return ResponseEntity.badRequest().build();
        }
        Norme norme = normeService.saveNorme(mapper.map(normeDto, Norme.class));
        return ResponseEntity.ok(mapper.map(norme, NormeDto.class));
    }

    /**
     * DELETE  /api/normes/{id} to delete the given norme
     */
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        LOGGER.debug("REST request to delete Norme with id {}", id);
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
        LOGGER.debug("REST request to update Norme");
        if (normeDto.getId() == null) {
            return ResponseEntity.badRequest().build();
        }
        Norme norme = normeService.saveNorme(mapper.map(normeDto, Norme.class));
        return ResponseEntity.ok(mapper.map(norme, NormeDto.class));
    }
}
