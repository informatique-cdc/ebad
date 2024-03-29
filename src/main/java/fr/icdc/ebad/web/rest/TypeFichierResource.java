package fr.icdc.ebad.web.rest;

import com.querydsl.core.types.Predicate;
import fr.icdc.ebad.domain.TypeFichier;
import fr.icdc.ebad.service.TypeFichierService;
import fr.icdc.ebad.web.rest.dto.TypeFichierDto;
import fr.icdc.ebad.mapper.MapStructMapper;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;

@RestController
@RequestMapping("/file-kinds")
@Tag(name = "TypeFichier", description = "the typefichier API")
public class TypeFichierResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(TypeFichierResource.class);

    private final TypeFichierService typeFichierService;
    private final MapStructMapper mapStructMapper;

    public TypeFichierResource(TypeFichierService typeFichierService, MapStructMapper mapStructMapper) {
        this.typeFichierService = typeFichierService;
        this.mapStructMapper = mapStructMapper;
    }


    /**
     * GET  /typefichier/application/:app to get all file kind from app.
     */
    @GetMapping(value = "/application/{app}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("@permissionApplication.canRead(#app,principal) or @permissionApplication.canWrite(#app,principal)")
    @PageableAsQueryParam
    public ResponseEntity<Page<TypeFichierDto>> getAllFromEnv(@Parameter(hidden = true) Pageable pageable, @QuerydslPredicate(root = TypeFichier.class) Predicate predicate, @PathVariable Long app) throws URISyntaxException {
        LOGGER.debug("REST request to get all TypeFichier from application {}", app);
        Page<TypeFichier> pageTypeFichier = typeFichierService.getTypeFichierFromApplication(predicate, pageable, app);

        Page<TypeFichierDto> pageTypeFichierDto = pageTypeFichier.map(mapStructMapper::convert);
        return new ResponseEntity<>(pageTypeFichierDto, HttpStatus.OK);
    }

    /**
     * PUT  /typefichier to add a new file type
     */
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("@permissionApplication.canWrite(#typeFichierDto.application.id,principal)")
    public ResponseEntity<TypeFichierDto> addTypeFichier(@RequestBody TypeFichierDto typeFichierDto) {
        LOGGER.debug("REST request to add a new type fichier");
        TypeFichier typeFichier = typeFichierService.saveTypeFichier(mapStructMapper.convert(typeFichierDto));
        return new ResponseEntity<>(mapStructMapper.convert(typeFichier), HttpStatus.OK);
    }

    /**
     * POST  /typefichier/delete to delete a fichier type
     */
    //FIXME SET DELETE MAPPING, WE CANT TRUST application.id in input DTO
    @PostMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("@permissionApplication.canWrite(#typeFichierDto.application.id,principal)")
    public ResponseEntity<Void> removeTypeFichier(@RequestBody TypeFichierDto typeFichierDto) {
        LOGGER.debug("REST request to remove a  type fichier");
        typeFichierService.deleteTypeFichier(typeFichierDto.getId());
        return new ResponseEntity<>(HttpStatus.OK);
    }


    /**
     * PATCH  /typefichier to update a fichier type&
     */
    @PatchMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("@permissionApplication.canWrite(#typeFichierDto.application.id,principal)")
    public ResponseEntity<TypeFichierDto> updateTypeFichier(@RequestBody TypeFichierDto typeFichierDto) {
        LOGGER.debug("REST request to update a type fichier");
        TypeFichier typeFichier = typeFichierService.saveTypeFichier(mapStructMapper.convert(typeFichierDto));
        return new ResponseEntity<>(mapStructMapper.convert(typeFichier), HttpStatus.OK);
    }
}
