package fr.icdc.ebad.service;

import com.querydsl.core.types.Predicate;
import fr.icdc.ebad.domain.Directory;
import fr.icdc.ebad.domain.QDirectory;
import fr.icdc.ebad.repository.DirectoryRepository;
import fr.icdc.ebad.service.util.EbadServiceException;
import fr.icdc.ebad.web.rest.dto.DirectoryDto;
import fr.icdc.ebad.web.rest.dto.FilesDto;
import ma.glasnost.orika.MapperFacade;
import org.apache.sshd.sftp.client.SftpClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dtrouillet on 03/03/2016.
 */
@Service
public class DirectoryService {
    private final DirectoryRepository directoryRepository;
    private final ShellService shellService;
    private final MapperFacade mapper;

    public DirectoryService(DirectoryRepository directoryRepository, ShellService shellService, MapperFacade mapper) {
        this.directoryRepository = directoryRepository;
        this.shellService = shellService;
        this.mapper = mapper;
    }

    @Transactional
    public List<FilesDto> listAllFiles(Long idDirectory, String subDirectory) throws EbadServiceException {
        List<FilesDto> filesDtoList = new ArrayList<>();
        Directory directory = directoryRepository.getById(idDirectory);

        List<SftpClient.DirEntry> files = shellService.getListFiles(directory, subDirectory);
        files
                .stream()
                .filter(file -> !".".equals(file.getFilename()) && !"..".equals(file.getFilename()))
                .filter(file -> {
                    if (file.getAttributes().isDirectory()) {
                        return directory.isCanExplore();
                    }
                    return true;
                })
                .forEach(file ->
                    filesDtoList.add(new FilesDto(mapper.map(directory, DirectoryDto.class), file.getFilename(), file.getAttributes().getSize(), LocalDateTime.ofInstant(file.getAttributes().getModifyTime().toInstant(), ZoneId.systemDefault()), LocalDateTime.ofInstant(file.getAttributes().getModifyTime().toInstant(), ZoneId.systemDefault()), file.getAttributes().isDirectory(), subDirectory))
                );

        return filesDtoList;
    }

    @Transactional
    public void removeFile(FilesDto filesDTO) throws EbadServiceException {
        Directory directory = directoryRepository.getById(filesDTO.getDirectory().getId());
        if (!directory.isCanWrite()) {
            throw new IllegalAccessError("Pas de permission pour supprimer ce fichier");
        }
        shellService.removeFile(directory, filesDTO.getName(), filesDTO.getSubDirectory());
    }

    @Transactional
    public InputStream readFile(FilesDto filesDTO) throws EbadServiceException {
        Directory directory = directoryRepository.getById(filesDTO.getDirectory().getId());
        return shellService.getFile(directory, filesDTO.getName(), filesDTO.getSubDirectory());
    }

    @Transactional
    public void uploadFile(MultipartFile multipartFile, Long directoryId, String subDirectory) throws EbadServiceException {
        Directory directory = getDirectory(directoryId);
        FilesDto filesDTO = new FilesDto(mapper.map(directory, DirectoryDto.class), multipartFile.getOriginalFilename(), 0L, LocalDateTime.now(), LocalDateTime.now(), false, subDirectory);

        if (filesDTO.getDirectory() == null) {
            throw new IllegalAccessError("Pas de permission pour supprimer ce fichier");
        }

        try {
            shellService.uploadFile(directory, multipartFile.getInputStream(), filesDTO.getName(), filesDTO.getSubDirectory());
        } catch (IOException e) {
            throw new EbadServiceException("Erreur lors de l'écriture d'un fichier du dossiers " + filesDTO.getDirectory().getName(), e);
        }
    }

    @Transactional(readOnly = true)
    public Directory getDirectory(Long id) {
        return directoryRepository.getById(id);
    }

    @Transactional(readOnly = true)
    public Page<Directory> findDirectoryFromEnvironnement(Predicate predicate, Pageable pageable, Long environnementId) {
        Predicate newPredicate = QDirectory.directory.environnement.id.eq(environnementId).and(predicate);
        return directoryRepository.findAll(newPredicate, pageable);
    }

    @Transactional
    public Directory saveDirectory(Directory directory) {
        return directoryRepository.save(directory);
    }

    @Transactional
    public void deleteDirectory(Long id) {
        directoryRepository.deleteById(id);
    }
}
