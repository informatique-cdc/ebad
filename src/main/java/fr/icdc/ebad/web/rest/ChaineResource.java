package fr.icdc.ebad.web.rest;

import com.jcraft.jsch.JSchException;
import fr.icdc.ebad.domain.Chaine;
import fr.icdc.ebad.domain.Environnement;
import fr.icdc.ebad.domain.util.RetourBatch;
import fr.icdc.ebad.service.ChaineService;
import fr.icdc.ebad.service.util.EbadServiceException;
import fr.icdc.ebad.web.rest.dto.ChaineDto;
import fr.icdc.ebad.web.rest.dto.ChaineSimpleDto;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("/chaines")
@Tag(name = "Chaines", description = "the chaines API")
public class ChaineResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChaineResource.class);

    private final ChaineService chaineService;
    private final MapperFacade mapper;

    public ChaineResource(ChaineService chaineService, MapperFacade mapper) {
        this.chaineService = chaineService;
        this.mapper = mapper;
    }

    /**
     * GET  /chaines/env/:env to get all chaine from env.
     */
    @GetMapping(value = "/env/{env}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("@permissionEnvironnement.canRead(#env,principal) or @permissionEnvironnement.canWrite(#env,principal)")
    public ResponseEntity<List<ChaineDto>> getAllFromEnv(@RequestParam(value = "page", required = false) Integer offset, @RequestParam(value = "per_page", required = false) Integer limit, @PathVariable Long env) throws URISyntaxException {
        LOGGER.debug("REST request to get all Chaines from environnement {}", env);
        Environnement environnement = Environnement.builder().id(env).build();
        Page<Chaine> page = chaineService.getAllChaineFromEnvironmentWithPageable(environnement, PaginationUtil.generatePageRequest(offset, limit));
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/chaines/env/" + env, offset, limit);
        return new ResponseEntity<>(mapper.mapAsList(page.getContent(), ChaineDto.class), headers, HttpStatus.OK);
    }

    /**
     * GET  /chaines/run/:id to run chaine
     */
    @PreAuthorize("@permissionEnvironnement.canRead(#env,principal)")
    @GetMapping(value = "/run/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<RetourBatch> runChaine(@PathVariable Long id, @RequestParam Long env) throws EbadServiceException {
        LOGGER.debug("REST request to run chaine");
        try {
            return new ResponseEntity<>(chaineService.runChaine(id), HttpStatus.OK);
        } catch (JSchException | IOException e) {
            LOGGER.error("Erreur lors de l'exécution du chaine", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * PUT  /chaines to add a new chaine
     */
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("@permissionEnvironnement.canWrite(#chaineDto.environnement.id,principal)")
    public ChaineSimpleDto addChaine(@RequestBody ChaineDto chaineDto) {
        LOGGER.debug("REST request to add a new chaine");
        Chaine chaine = chaineService.addChaine(mapper.map(chaineDto, Chaine.class));
        return mapper.map(chaine, ChaineSimpleDto.class);
    }

    /**
     * DELETE  /chaines/delete/{id} to delete a chaine with given id
     */
    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("@permissionChaine.canWrite(#id,principal)")
    public ResponseEntity<Void> removeChaine(@PathVariable Long id) {
        LOGGER.debug("REST request to remove a chaine");
        chaineService.deleteChaine(id);
        return ResponseEntity.ok().build();
    }

    /**
     * PATCH  /chaines to update a chaine
     */
    @PatchMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("@permissionChaine.canWrite(#chaineDto,principal)")
    public ResponseEntity<ChaineSimpleDto> updateChaine(@RequestBody ChaineDto chaineDto) {
        LOGGER.debug("REST request to update a chaine");
        Chaine chaine = chaineService.updateChaine(mapper.map(chaineDto, Chaine.class));
        return new ResponseEntity<>(mapper.map(chaine, ChaineSimpleDto.class), HttpStatus.OK);
    }
}
