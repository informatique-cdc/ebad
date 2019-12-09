package fr.icdc.ebad.web.rest;

import fr.icdc.ebad.domain.TypeFichier;
import fr.icdc.ebad.service.TypeFichierService;
import fr.icdc.ebad.web.rest.dto.TypeFichierDto;
import fr.icdc.ebad.web.rest.util.PaginationUtil;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.tags.Tag;
import ma.glasnost.orika.MapperFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("/typefichier")
@Tag(name = "TypeFichier", description = "the typefichier API")
public class TypeFichierResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(TypeFichierResource.class);

    private final TypeFichierService typeFichierService;
    private final MapperFacade mapper;

    public TypeFichierResource(TypeFichierService typeFichierService, MapperFacade mapper) {
        this.typeFichierService = typeFichierService;
        this.mapper = mapper;
    }


    /**
     * GET  /typefichier/application/:app to get all file kind from app.
     */
    @GetMapping(value = "/application/{app}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("@permissionApplication.canRead(#app,principal) or @permissionApplication.canWrite(#app,principal)")
    public ResponseEntity<List<TypeFichierDto>> getAllFromEnv(@RequestParam(value = "page", required = false) Integer offset, @RequestParam(value = "per_page", required = false) Integer limit, @PathVariable Long app) throws URISyntaxException {
        LOGGER.debug("REST request to get all TypeFichier from application {}", app);
        Page<TypeFichier> pageTypeFichier = typeFichierService.getTypeFichierFromApplication(PaginationUtil.generatePageRequest(offset, limit), app);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(pageTypeFichier, "/typefichier/application/" + app, offset, limit);

        Page<TypeFichierDto> pageTypeFichierDto = pageTypeFichier.map(typeFichier ->
                mapper.map(typeFichier, TypeFichierDto.class)
        );
        return new ResponseEntity<>(pageTypeFichierDto.getContent(), headers, HttpStatus.OK);
    }

    /**
     * PUT  /typefichier to add a new file type
     */
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("@permissionApplication.canWrite(#typeFichierDto.application.id,principal)")
    public ResponseEntity<TypeFichierDto> addTypeFichier(@RequestBody TypeFichierDto typeFichierDto) {
        LOGGER.debug("REST request to add a new type fichier");
        TypeFichier typeFichier = typeFichierService.saveTypeFichier(mapper.map(typeFichierDto, TypeFichier.class));
        return new ResponseEntity<>(mapper.map(typeFichier, TypeFichierDto.class), HttpStatus.OK);
    }

    /**
     * POST  /typefichier/delete to delete a fichier type
     */
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
        TypeFichier typeFichier = typeFichierService.saveTypeFichier(mapper.map(typeFichierDto, TypeFichier.class));
        return new ResponseEntity<>(mapper.map(typeFichier, TypeFichierDto.class), HttpStatus.OK);
    }
}
