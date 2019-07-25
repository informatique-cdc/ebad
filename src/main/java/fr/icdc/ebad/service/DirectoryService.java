package fr.icdc.ebad.service;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import fr.icdc.ebad.domain.Directory;
import fr.icdc.ebad.repository.DirectoryRepository;
import fr.icdc.ebad.service.util.EbadServiceException;
import fr.icdc.ebad.web.rest.dto.FilesDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dtrouillet on 03/03/2016.
 */
@Service
public class DirectoryService {
    private final DirectoryRepository directoryRepository;
    private final ShellService shellService;

    public DirectoryService(DirectoryRepository directoryRepository, ShellService shellService) {
        this.directoryRepository = directoryRepository;
        this.shellService = shellService;
    }

    @Transactional
    public List<FilesDto> listAllFiles(Long idDirectory) throws EbadServiceException {
        List<FilesDto> filesDtoList = new ArrayList<>();
        Directory directory = directoryRepository.getOne(idDirectory);
        try {
            List<ChannelSftp.LsEntry> files = shellService.getListFiles(directory);
            files.stream().filter(file -> !".".equals(file.getFilename()) && !"..".equals(file.getFilename())).forEach(file -> filesDtoList.add(new FilesDto(directory, file.getFilename(), file.getAttrs().getSize(), file.getAttrs().getATime(), file.getAttrs().getMTime())));
        } catch (SftpException | JSchException e) {
            throw new EbadServiceException("Impossible de lister les fichiers sur le serveur distant du répertoire " + directory.getName(), e);
        }
        return filesDtoList;
    }

    @Transactional
    public void removeFile(FilesDto filesDTO) throws EbadServiceException {
        Directory directory = directoryRepository.getOne(filesDTO.getDirectory().getId());
        if (!directory.isCanWrite()) {
            throw new IllegalAccessError("Pas de permission pour supprimer ce fichier");
        }

        try {
            shellService.removeFile(directory, filesDTO.getName());
        } catch (SftpException | JSchException e) {
            throw new EbadServiceException("Impossible de supprimer le fichier " + filesDTO.getName());
        }
    }

    @Transactional
    public InputStream readFile(FilesDto filesDTO) throws EbadServiceException {
        Directory directory = directoryRepository.getOne(filesDTO.getDirectory().getId());
        try {
            return shellService.getFile(directory, filesDTO.getName());
        } catch (SftpException | JSchException | IOException e) {
            throw new EbadServiceException("Impossible de lire le fichier " + filesDTO.getName());

        }
    }

    public void uploadFile(InputStream stream, FilesDto filesDTO) throws EbadServiceException {
        if (filesDTO.getDirectory() == null) {
            throw new IllegalAccessError("Pas de permission pour supprimer ce fichier");
        }

        try {
            shellService.uploadFile(filesDTO.getDirectory(), stream, filesDTO.getName());
        } catch (SftpException | JSchException e) {
            throw new EbadServiceException("Erreur lors de l'écriture d'un fichier du dossiers " + filesDTO.getDirectory().getName());
        }
    }

    @Transactional(readOnly = true)
    public Directory getDirectory(Long id) {
        return directoryRepository.getOne(id);
    }

    @Transactional(readOnly = true)
    public Page<Directory> findDirectoryFromEnvironnement(Long environnementId, Pageable pageable) {
        return directoryRepository.findDirectoryFromEnvironnement(pageable, environnementId);
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
