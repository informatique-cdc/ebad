package fr.icdc.ebad.web.rest;

import com.querydsl.core.types.Predicate;
import fr.icdc.ebad.domain.Directory;
import fr.icdc.ebad.service.DirectoryService;
import fr.icdc.ebad.service.util.EbadServiceException;
import fr.icdc.ebad.web.rest.dto.DirectoryDto;
import fr.icdc.ebad.web.rest.dto.FilesDto;
import fr.icdc.ebad.mapper.MapStructMapper;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("/directories")
@Tag(name = "Directory", description = "the directory API")
public class DirectoryResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryResource.class);

    private final DirectoryService directoryService;
    private final MapStructMapper mapStructMapper;

    public DirectoryResource(DirectoryService directoryService, MapStructMapper mapStructMapper) {
        this.directoryService = directoryService;
        this.mapStructMapper = mapStructMapper;
    }

    /**
     * GET  /dossiers/env/:env to get all directories from env.
     */
    @GetMapping(value = "/env/{env}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("@permissionEnvironnement.canRead(#env, principal) or @permissionEnvironnement.canWrite(#env, principal)")
    @PageableAsQueryParam
    public ResponseEntity<Page<DirectoryDto>> getAllFromEnv(@Parameter(hidden = true) Pageable pageable, @QuerydslPredicate(root = Directory.class) Predicate predicate, @PathVariable Long env) throws URISyntaxException {
        LOGGER.debug("REST request to get all Directory from environnement {}", env);
        Page<Directory> page = directoryService.findDirectoryFromEnvironnement(predicate, pageable, env);
        Page<DirectoryDto> directoryDtos = page.map(mapStructMapper::convert);

        return new ResponseEntity<>(directoryDtos, HttpStatus.OK);
    }

    /**
     * GET  /dossiers/files/{id} to get all files from directory.
     */
    @GetMapping(value = "/files/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("@permissionDirectory.canRead(#id, #subDirectory, principal)")
    public ResponseEntity<List<FilesDto>> getFilesFromDirectory(@RequestParam(value = "page", required = false) Integer offset, @RequestParam(value = "per_page", required = false) Integer limit, @PathVariable Long id, @RequestParam(required = false) String subDirectory) throws EbadServiceException {
        LOGGER.debug("REST request to get all files from directory {}", id);
        return new ResponseEntity<>(directoryService.listAllFiles(id, subDirectory), HttpStatus.OK); //TODO DTROUILLET GESTION DES ERREURS
    }

    /**
     * POST  /dossiers/files/ to remove file from directory.
     */
    @PostMapping(value = "/files/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("@permissionDirectory.canWriteFile(#filesDTO.directory.id, #filesDTO.subDirectory, principal)")
    public ResponseEntity<Void> removeFileFromDirectory(@RequestBody FilesDto filesDTO) throws EbadServiceException {
        LOGGER.debug("REST request to remove file from directory {}", filesDTO.getName());
        try {
            directoryService.removeFile(filesDTO);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IllegalAccessError e) {
            LOGGER.error("Erreur lors de la suppression du fichier {}", filesDTO.getName(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping(value = "/files/read")
    @ApiResponses(value = {
            @ApiResponse(content = @Content(schema = @Schema(type = "string", format = "binary")))
    })
    @GetMapping("/{id}")
    @PreAuthorize("@permissionDirectory.canRead(#filesDTO.directory.id, #filesDTO.subDirectory, principal)")
    public void downloadFile(@RequestBody FilesDto filesDTO, HttpServletResponse httpServletResponse) throws EbadServiceException {
        LOGGER.debug("REST request to read file from directory {}", filesDTO.getName());
        try {
            InputStream inputStream = directoryService.readFile(filesDTO);
            org.apache.commons.io.IOUtils.copy(inputStream, httpServletResponse.getOutputStream());
            httpServletResponse.flushBuffer();
            inputStream.close();
        } catch (IllegalAccessError | IllegalStateException | IOException e) {
            LOGGER.error("Erreur lors de la lecture du fichier {}", filesDTO.getName(), e);
        }
    }


    @PostMapping(value = "/files/upload", consumes = "multipart/form-data")
    @PreAuthorize("@permissionDirectory.canWriteFile(#directory, #subDirectory, principal)")
    public ResponseEntity<Void> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("directory") Long directory, @RequestParam(value = "subDirectory", required = false) String subDirectory) throws EbadServiceException {
        LOGGER.debug("REST request to write file to directory {}", directory);
        if (!file.isEmpty()) {
            directoryService.uploadFile(file, directory, subDirectory);
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * PUT  /directories to add a new directory
     */
    @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("@permissionDirectory.canWrite(#directoryDto, principal)")
    public ResponseEntity<DirectoryDto> addDirectory(@RequestBody DirectoryDto directoryDto) {
        LOGGER.debug("REST request to add a new directory");
        Directory directory = directoryService.saveDirectory(mapStructMapper.convert(directoryDto));
        return new ResponseEntity<>(mapStructMapper.convert(directory), HttpStatus.OK);
    }

    /**
     * POST  /directories/delete to delete a directory
     */
    @PostMapping(value = "/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("@permissionDirectory.canWrite(#directoryDto, principal)")
    public ResponseEntity<Void> removeDirectory(@RequestBody DirectoryDto directoryDto) {
        LOGGER.debug("REST request to remove a  directory");
        directoryService.deleteDirectory(directoryDto.getId());
        return new ResponseEntity<>(HttpStatus.OK);
    }


    /**
     * PATCH  /directories to update a directory
     */
    @PatchMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @PreAuthorize("@permissionDirectory.canWrite(#directoryDto, principal)")
    public ResponseEntity<DirectoryDto> updateDirectory(@RequestBody DirectoryDto directoryDto) {
        LOGGER.debug("REST request to update a directory");
        Directory directory = directoryService.saveDirectory(mapStructMapper.convert(directoryDto));
        return new ResponseEntity<>(mapStructMapper.convert(directory), HttpStatus.OK);
    }
}
